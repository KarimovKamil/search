INSERT INTO terms_list (term_id, term_text)
  SELECT DISTINCT ON (m.term)
    m.id,
    m.term
  FROM words_mystem m;

INSERT INTO article_term (
  SELECT
    m.articles_id :: UUID,
    (SELECT t.term_id
     FROM terms_list t
     WHERE term_text = m.term)
  FROM words_mystem m);