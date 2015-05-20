Delete From `persons_mview`;

Insert into `persons_mview`
	 Select distinct
         `persons_label`.item_id as person_ID,
         `persons_label`.label as name,
         `jobs_label`.item_id as job_ID,
         `jobs_label`.label as job_title,
		 `persons_link`.url as wikipedia_hyperlink, 
		 `persons_label`.language as language 
		
		From `persons_label`
			Join `persons_claim` 				ON `persons_label`.item_id = `persons_claim`.item_id 	AND `persons_claim`.property = "P106" -- Beruf
             Join `jobs_label`				ON `persons_claim`.value = `jobs_label`.item_id			AND( `persons_label`.language = `jobs_label`.language OR `persons_label`.language = "en" )
            Left Join `persons_link`			ON `persons_link`.item_id = `persons_label`.item_id 	AND `persons_label`.language = `persons_link`.language;
