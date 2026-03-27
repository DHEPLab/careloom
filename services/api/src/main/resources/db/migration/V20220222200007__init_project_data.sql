update baby set project_id=(select id from project where code='health_future');

update baby_history set project_id=(select id from project where code='health_future');

update carer set project_id=(select id from project where code='health_future');

update carer_history set project_id=(select id from project where code='health_future');

update community_house_worker set project_id=(select id from project where code='health_future');

update community_house_worker_history set project_id=(select id from project where code='health_future');

update curriculum set project_id=(select id from project where code='health_future');

update curriculum_history set project_id=(select id from project where code='health_future');

update lesson set project_id=(select id from project where code='health_future');

update lesson_history set project_id=(select id from project where code='health_future');

update module set project_id=(select id from project where code='health_future');

update module_history set project_id=(select id from project where code='health_future');

update questionnaire set project_id=(select id from project where code='health_future');

update questionnaire_history set project_id=(select id from project where code='health_future');

update user set project_id=(select id from project where code='health_future');

update user_history set project_id=(select id from project where code='health_future');

update visit set project_id=(select id from project where code='health_future');

update visit_history set project_id=(select id from project where code='health_future')
