ALTER TABLE experience_program
    ADD FULLTEXT INDEX idx_program_title_business_name_fulltext
        (title, business_name)
        WITH PARSER ngram;
