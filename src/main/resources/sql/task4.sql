ALTER TABLE article_term
  ADD COLUMN IF NOT EXISTS word_count INT;
ALTER TABLE article_term
  ADD COLUMN IF NOT EXISTS tf_idf FLOAT;

UPDATE article_term at
SET at.tf_idf =
((word_count :: FLOAT /
  (SELECT sum(word_count)
   FROM article_term a
   WHERE a.article_id = at.article_id) :: FLOAT) *
 ((log(2, ((SELECT count(article_id)
            FROM article_term))
          - log(2, (SELECT count(article_id)
                    FROM article_term
                    WHERE EXISTS(SELECT 1
                                 FROM article_term a1
                                 WHERE a1.term_id = at.term_id))))) :: FLOAT));

UPDATE article_term
SET tf_idf = sub.tfidf FROM
  (SELECT
     ((word_count :: FLOAT /
       (SELECT sum(word_count)
        FROM article_term a
        WHERE a.article_id = aa.article_id) :: FLOAT) *
      ((log(2, ((SELECT count(article_id)
                 FROM article_term))
               - log(2, (SELECT count(article_id)
                         FROM article_term
                         WHERE EXISTS(SELECT 1
                                      FROM article_term a1
                                      WHERE a1.term_id = aa.term_id))))) :: FLOAT)) AS tfidf,
     aa.term_id,
     aa.article_id
   FROM article_term aa) AS sub
WHERE article_term.term_id = sub.term_id AND article_term.article_id = sub.article_id;

SELECT
  ((word_count :: FLOAT /
    (SELECT sum(word_count)
     FROM article_term a
     WHERE a.article_id = aa.article_id) :: FLOAT) *
   ((log(2, ((SELECT count(article_id)
              FROM article_term))
            - log(2, (SELECT count(article_id)
                      FROM article_term
                      WHERE EXISTS(SELECT 1
                                   FROM article_term a1
                                   WHERE a1.term_id = aa.term_id))))) :: FLOAT)) AS tfidf,
  aa.term_id,
  aa.article_id
FROM article_term aa