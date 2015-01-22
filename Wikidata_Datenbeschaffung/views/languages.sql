Delete from `languages_help`;

INSERT INTO `languages_help`  (language, text) SELECT LOWER(language), text FROM `iso_languages`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `jobs_alias`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `jobs_desc`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `jobs_label`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `jobs_link`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `persons_alias`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `persons_desc`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `persons_label`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `persons_link`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `educationInstitutes_alias`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `educationInstitutes_desc`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `educationInstitutes_label`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `educationInstitutes_link`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `cities_alias`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `cities_desc`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `cities_label`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `cities_link`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `states_alias`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `states_desc`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `states_label`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `states_link`;
INSERT INTO `languages_help`  (language) SELECT DISTINCT LOWER(language) FROM `gui_texts`;


Delete From `languages`;


Insert Into `languages`
	Select language, text 
		From languages_help
		Group by language;
