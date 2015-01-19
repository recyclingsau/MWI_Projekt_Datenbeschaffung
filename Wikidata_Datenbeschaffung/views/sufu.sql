Delete From `sufu_mview`;
  
Insert into `sufu_mview`
	 Select distinct `states_label`.item_id as state_ID, `cities_label`.item_id as city_ID, `educationinstitutes_claim`.item_id as uni_ID, `persons_claim`.item_id as person_ID, `jobs_label`.item_id as job_ID
		From `educationinstitutes_claim`
			Left Join `educationinstitutes_label`	On `educationinstitutes_claim`.item_id = `educationinstitutes_label`.item_id
            Left Join `states_label`				On `educationinstitutes_claim`.value = `states_label`.item_id 		OR `educationinstitutes_claim`.value = `states_label`.label		AND `educationinstitutes_claim`.property = "P17" -- Land
            Left Join `cities_label`				On `educationinstitutes_claim`.value = `cities_label`.item_id 		OR `educationinstitutes_claim`.value = `cities_label`.label		AND `educationinstitutes_claim`.property = "P1134" -- Stadt
            Left Join `persons_claim` 				On `persons_claim`.value = `educationinstitutes_claim`.item_id 		OR `persons_claim`.value = `educationinstitutes_label`.label 	AND `persons_claim`.property = "P69" -- Alma Marter 
            Left Join `jobs_label` 					On `persons_claim`.value = `jobs_label`.item_id						OR `persons_claim`.value = `jobs_label`.label					AND `persons_claim`.property = "P106"; -- Beruf
