Delete from persons_mview;

Insert into persons_mview
	Select persons_label.label as Name, jobs_label.label as jobs_title, persons_link.url as wikipedia_hyperlink, persons_label.language as language, persons_label.item_id as person_ID, jobs_label.item_id as job_ID
		From persons_label
			Left Join persons_claim ON persons_label.item_id = persons_claim.item_id 	AND persons_claim.property = "P106"
            Left Join jobs_label 	ON persons_claim.value = jobs_label.item_id 		AND persons_label.language = jobs_label.language
            Left Join persons_link 	ON persons_link.item_id = persons_label.item_id 	AND persons_label.language = persons_link.language;
