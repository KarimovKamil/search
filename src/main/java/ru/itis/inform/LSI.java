package ru.itis.inform;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.Request;
import scala.Option;

import java.util.*;

public class LSI {
    private static final int K = 5;

    private String[] articleIds;
    private String[] terms;
    private double[][] matrix;

    public LSI() {
        PageDao pageDao = new PageDao();
        WordsDao wordsDao = new WordsDao();

        Map<String, Map<String, Integer>> termsWithArticleIds = wordsDao.getTermsWithArticleIds();

        articleIds = pageDao.getAll().stream().map(PageInfo::getId).toArray(String[]::new);
        terms = termsWithArticleIds.keySet().toArray(new String[0]);

        matrix = new double[terms.length][articleIds.length];

        for (int i = 0; i < terms.length; i++) {
            Map<String, Integer> article = termsWithArticleIds.get(terms[i]);
            for (int j = 0; j < articleIds.length; j++) {
                matrix[i][j] = article.getOrDefault(articleIds[j], 0);
            }
        }
    }

    public static void main(String[] args) {
        LSI lsi = new LSI();
        String[] articleIds = lsi.getArticleIds();
        String[] terms = lsi.getTerms();
        double[][] sourceMatrix = lsi.getMatrix();

        int termsCount = terms.length;
        int documentsCount = articleIds.length;

        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        String[] words = input.split(" ");
        MyStem mystemAnalyzer =
                new Factory("-igd --eng-gr --format json --weight")
                        .newMyStem("3.0", Option.empty()).get();

        Set<String> wordsInQ = new HashSet<>();
        for (String word : words) {
            try {
                wordsInQ.add(mystemAnalyzer.analyze(Request.apply(word))
                        .info().head().lex().get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double[][] query = new double[1][termsCount];
        for (int i = 0; i < termsCount; i++) {
            if (wordsInQ.contains(terms[i])) {
                query[0][i] = 1;
            }
        }

        Matrix matrix = new Matrix(sourceMatrix);
        Matrix q = new Matrix(query);

        SingularValueDecomposition svd = matrix.svd();

        Matrix u = svd.getU();
        Matrix s = svd.getS();
        Matrix vt = svd.getV().transpose();

        Matrix uk = u.getMatrix(0, termsCount - 1, 0, K - 1);
        Matrix sk = s.getMatrix(0, K - 1, 0, K - 1);
        Matrix vtk = vt.getMatrix(0, K - 1, 0, documentsCount - 1);

        Matrix result = q.times(uk).times(sk.inverse());

        Map<String, Double> output = new HashMap<>();
        PageDao pageDao = new PageDao();
        for (int i = 0; i < documentsCount; i++) {
            Matrix d = vtk.getMatrix(0, K - 1, i, i).transpose();
            output.put(pageDao.getPageUrlById(articleIds[i]), cosine(result.getArrayCopy()[0], d.getArrayCopy()[0]));
        }

        List<Map.Entry<String, Double>> list = new ArrayList<>(output.entrySet());
        list.sort(Comparator.comparingDouble(Map.Entry::getValue));

        for (int i = 29; i > 19; i--) {
            System.out.println(list.get(i).getKey() + ": " + list.get(i).getValue());

        }
    }

    public static double cosine(double[] q, double[] d) {
        double a2 = 0;
        double b2 = 0;
        double ab = 0;
        for (int i = 0; i < K; i++) {
            ab += (q[i] * d[i]);
            b2 += (q[i] * q[i]);
            a2 += (d[i] * d[i]);
        }
        return ab / Math.sqrt(a2) / Math.sqrt(b2);
    }

    public String[] getArticleIds() {
        return articleIds;
    }

    public String[] getTerms() {
        return terms;
    }

    public double[][] getMatrix() {
        return matrix;
    }
}
