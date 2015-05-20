Delete From `sufu_mview`;
  
Insert into `sufu_mview`
	 Select distinct 
	 `states_label`.item_id as state_ID,
	 `cities_label`.item_id as city_ID, 
	 `educationinstitutes_claim1`.item_id as uni_ID, 
	 `persons_claim1`.item_id as person_ID, 
	 `jobs_label`.item_id as job_ID
	 
		From `educationinstitutes_claim` as `educationinstitutes_claim1`
            Join `educationinstitutes_claim` as `educationinstitutes_claim2`		On `educationinstitutes_claim1`.item_id = `educationinstitutes_claim2`.item_id 		AND `educationinstitutes_claim2`.property = "P276" -- Stadt
            Join `states_label`														On `educationinstitutes_claim1`.value = `states_label`.item_id 						AND `educationinstitutes_claim1`.property = "P17" -- Land
            Join `cities_label`														On `educationinstitutes_claim2`.value = `cities_label`.item_id 					 	AND `educationinstitutes_claim2`.property = "P276" -- Stadt
            Left Join `persons_claim` as `persons_claim1`							On `persons_claim1`.value = `educationinstitutes_claim1`.item_id					AND `persons_claim1`.property = "P69" -- Alma Marter
            Left Join `persons_claim` as `persons_claim2`							On `persons_claim1`.item_id = `persons_claim2`.item_id			 					AND `persons_claim2`.property = "P106" -- Beruf
            Left Join `jobs_label` 													On `persons_claim2`.value = `jobs_label`.item_id								 	AND `persons_claim2`.property = "P106"; -- Beruf
