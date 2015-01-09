Delete from sufu_mview;

Insert into sufu_mview
	Select states_claim.item_id as state_ID, cities_label.item_id as city_ID, educationinstitutes_claim.item_id as uni_ID, persons_claim.item_id as person_ID, jobs_claim.item_id as job_ID
		From educationinstitutes_claim
			Left Join states_claim 												On educationinstitutes_claim.value = states_claim.item_id 					AND educationinstitutes_claim.property = "P17"
            Left Join educationinstitutes_claim as educationinstitutes_claim2 	On educationinstitutes_claim.item_id = educationinstitutes_claim2.item_id 	AND educationinstitutes_claim2.property = "P1134"
            Left JOIN cities_label												ON educationinstitutes_claim2.value = cities_label.item_id	
            Left Join persons_claim 											On persons_claim.value = educationinstitutes_claim.item_id 					AND persons_claim.property = "P69"
            Left Join jobs_claim 												On persons_claim.value = jobs_claim.item_id 								AND persons_claim.property = "P106";
