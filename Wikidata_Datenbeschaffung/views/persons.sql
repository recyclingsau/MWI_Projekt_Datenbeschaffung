Delete From `persons_mview`;

Insert into `persons_mview`
	 Select distinct `persons_label`.label as Name, 
		 if(persons_claim.value like "Q0%" or persons_claim.value like "Q1%" or persons_claim.value like "Q2%" or persons_claim.value like "Q3%" or persons_claim.value like "Q4%" or persons_claim.value like "Q5%" or persons_claim.value like "Q6%" or persons_claim.value like "Q7%" or persons_claim.value like "Q8%" or persons_claim.value like "Q9%",`jobs_label`.label,persons_claim.value) as jobs_title, 
		 `persons_link`.url as wikipedia_hyperlink, 
		 `persons_label`.language as language, 
		 `persons_label`.item_id as person_ID, 
		 `jobs_label`.item_id as job_ID
         
		From `persons_label`
			Left Join `persons_claim` 	ON `persons_label`.item_id = `persons_claim`.item_id 	AND `persons_claim`.property = "P106" -- Beruf
            Left Join `jobs_label` 		ON `persons_claim`.value = `jobs_label`.item_id 		OR `persons_claim`.value = `jobs_label`.label 		AND `persons_label`.language = `jobs_label`.language
            Left Join `persons_link`	ON `persons_link`.item_id = `persons_label`.item_id 	AND `persons_label`.language = `persons_link`.language;
  