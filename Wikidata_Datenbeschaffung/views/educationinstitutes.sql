delete from `educationinstitutes_mview`;

insert into `educationinstitutes_mview`
	 select distinct 
		`educationinstitutes_label`.item_id 	as id, 
		`educationinstitutes_label`.label 		as name, 
	 	SUBSTRING(`educationinstitutes_claim1`.value FROM (POSITION(',' IN `educationinstitutes_claim1`.value)+2) For 20) 	as longitude, 
		SUBSTRING(`educationinstitutes_claim1`.value FROM 1 FOR (POSITION(',' IN `educationinstitutes_claim1`.value)-1)) 	as latitude,
		`states_label`.label 					as state,
        `educationinstitutes_claim3`.value 		as zip_code,
        `educationinstitutes_claim4`.value 		as phone,
        `educationinstitutes_claim5`.value 		as email,
        `educationinstitutes_claim6`.value 		as year_of_foundation,
        `educationinstitutes_claim7`.value 		as street_and_house_number,
        `cities_label`.label 					as city,
        `educationinstitutes_desc`.description 	as education_institute_description,
        `educationinstitutes_link`.url 			as wikipedia_hyperlink,
        `educationinstitutes_label`.language 	as language
        
    from `educationinstitutes_label`
        Left JOIN `educationinstitutes_claim` as `educationinstitutes_claim1` 	ON `educationinstitutes_label`.item_id = `educationinstitutes_claim1`.item_id 	AND `educationinstitutes_claim1`.property = "P625" -- Geo Koords
		Left JOIN `educationinstitutes_claim` as `educationinstitutes_claim2` 	ON `educationinstitutes_label`.item_id = `educationinstitutes_claim2`.item_id 	AND `educationinstitutes_claim2`.property = "P17" -- Staat
        Left JOIN `states_label`												ON `educationinstitutes_claim2`.value = `states_label`.item_id					AND `states_label`.language = `educationinstitutes_label`.language
		Left JOIN `educationinstitutes_claim` as `educationinstitutes_claim3` 	ON `educationinstitutes_label`.item_id = `educationinstitutes_claim3`.item_id 	AND `educationinstitutes_claim3`.property = "P281" -- PLZ
        Left JOIN `educationinstitutes_claim` as `educationinstitutes_claim4` 	ON `educationinstitutes_label`.item_id = `educationinstitutes_claim4`.item_id 	AND `educationinstitutes_claim4`.property = "P1329" -- Tel
        Left JOIN `educationinstitutes_claim` as `educationinstitutes_claim5` 	ON `educationinstitutes_label`.item_id = `educationinstitutes_claim5`.item_id 	AND `educationinstitutes_claim5`.property = "P968" -- EMail
		Left JOIN `educationinstitutes_claim` as `educationinstitutes_claim6` 	ON `educationinstitutes_label`.item_id = `educationinstitutes_claim6`.item_id 	AND `educationinstitutes_claim6`.property = "P571" -- Gründungsdatum
        Left JOIN `educationinstitutes_claim` as `educationinstitutes_claim7` 	ON `educationinstitutes_label`.item_id = `educationinstitutes_claim7`.item_id 	AND `educationinstitutes_claim7`.property = "P969" -- Adresse
        Left JOIN `educationinstitutes_claim` as `educationinstitutes_claim8` 	ON `educationinstitutes_label`.item_id = `educationinstitutes_claim8`.item_id 	AND `educationinstitutes_claim8`.property = "P276" -- Stadt / Lage
		Left JOIN `cities_label`												ON `educationinstitutes_claim8`.value = `cities_label`.item_id 					AND( `cities_label`.language = `educationinstitutes_label`.language OR `cities_label`.language = "EN" )
		Left JOIN `educationinstitutes_link`									ON `educationinstitutes_label`.item_id = `educationinstitutes_link`.item_id 	AND `educationinstitutes_link`.language = `educationinstitutes_label`.language
        Left JOIN `educationinstitutes_desc` 									ON `educationinstitutes_label`.item_id = `educationinstitutes_desc`.item_id 	AND `educationinstitutes_desc`.language = `educationinstitutes_label`.language;
