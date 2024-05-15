INSERT INTO roles (id, name)
VALUES
    ('a56c2a6a-0212-4dde-a0d0-f016f0349498', 'ROLE_USER'),
    ('4637ad14-9bd2-435b-bb03-3dfebb658d31', 'ROLE_ADMIN');

-- Insert sample data into users table
INSERT INTO users (id, email, password, threshold, tokens, username,creation_date,update_date)
VALUES
    ('21841092-b56f-4dbf-ad1c-1190a231cdfb', 'admin@example.com', 'admin', 100, 50, 'admin',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    ('ec3c532d-8d3f-4be8-b583-c7c93dba044b', 'user@example.com', 'user', 150, 75, 'user',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    ('d2a8d77e-a72d-4288-bca7-bba6631b4166', 'user1@example.com', 'user1', 200, 100, 'user1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

-- Insert sample data into user_roles table
INSERT INTO user_roles (user_id, role_id)
VALUES
    ('21841092-b56f-4dbf-ad1c-1190a231cdfb', '4637ad14-9bd2-435b-bb03-3dfebb658d31'),
    ('ec3c532d-8d3f-4be8-b583-c7c93dba044b', 'a56c2a6a-0212-4dde-a0d0-f016f0349498'),
    ('d2a8d77e-a72d-4288-bca7-bba6631b4166', 'a56c2a6a-0212-4dde-a0d0-f016f0349498');

-- Insert sample data into badges table
INSERT INTO badges (id, name, creation_date, update_date)
VALUES
    ('e40d891f-1889-4449-8a27-ee1e4409f10a', 'Beginner', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('5d50ea00-0f5b-4a84-8f0c-2d728a256292', 'Intermediate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('28e3f575-87be-4e9f-a33a-7fd9390a0390', 'Expert', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample data into user_badges table
INSERT INTO user_badges (user_id, badge_id)
VALUES
    ('ec3c532d-8d3f-4be8-b583-c7c93dba044b', 'e40d891f-1889-4449-8a27-ee1e4409f10a'),
    ('ec3c532d-8d3f-4be8-b583-c7c93dba044b', '5d50ea00-0f5b-4a84-8f0c-2d728a256292'),
    ('21841092-b56f-4dbf-ad1c-1190a231cdfb', 'e40d891f-1889-4449-8a27-ee1e4409f10a');

-- Insert sample data into quests table
INSERT INTO quests (id, answer, description, difficulty, quest_reward_tokens, rewarded, threshold, creation_date, update_date)
VALUES
    ('58bf523d-5ae8-4ff1-8ddc-c722db53b517', 'Answer1', 'Description1', 'Easy', 10, true, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('40e2f3d1-e992-4c50-8dc3-849f984e635c', 'Answer2', 'Description2', 'Medium', 20, true, 75, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('24111738-48b8-42dc-a294-2eb3156d4002', 'Answer3', 'Description3', 'Hard', 30, true, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample data into quests_users table
INSERT INTO quests_users (user_id, quest_id)
VALUES
    ('ec3c532d-8d3f-4be8-b583-c7c93dba044b', '24111738-48b8-42dc-a294-2eb3156d4002'),
    ('ec3c532d-8d3f-4be8-b583-c7c93dba044b', '40e2f3d1-e992-4c50-8dc3-849f984e635c'),
    ('ec3c532d-8d3f-4be8-b583-c7c93dba044b', '58bf523d-5ae8-4ff1-8ddc-c722db53b517');

-- Inserting data into the categories table
INSERT INTO categories (name, creation_date, update_date)
VALUES
    ('Category 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Category 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Category 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO public.categories
(id, "name", creation_date, update_date)
VALUES
    ('c00be767-f747-4ac8-9e1d-1f7a3f0e4760', 'Society & Culture', NOW(), NOW()),
    ('3014a547-2f15-4c98-a376-b7683d5e9325', 'Science & Mathematics', NOW(), NOW()),
    ('1c924683-72f3-4a7f-bd0d-336aa5b2902a', 'Health', NOW(), NOW()),
    ('3071582d-c0ad-4213-8060-8527918d558e', 'Education & Reference', NOW(), NOW()),
    ('e32bef0e-79c5-4f09-85ba-783de3aafd69', 'Computers & Internet', NOW(), NOW()),
    ('52393790-e9f0-4025-8e27-b32a044bc2c0', 'Sports', NOW(), NOW()),
    ('a515d9e8-a636-465f-964c-6b740222b216', 'Business & Finance', NOW(), NOW()),
    ('a8425ba5-3d7d-4fb0-a711-8cc548906048', 'Entertainment & Music', NOW(), NOW()),
    ('16f269d2-af46-4e44-8b32-f13949976457', 'Family & Relationships', NOW(), NOW()),
    ('8b4d7863-21b6-4d5a-b62f-5236d1f80e0b', 'Politics & Government', NOW(), NOW());


-- Society & Culture Category
INSERT INTO public.questions
(id, question_text, answer1, answer2, answer3, correct_answer, difficulty, quest_reward_tokens, rewarded, threshold, category_id, creation_date, update_date, checked_by_admin)
VALUES
    ('a1bb3d19-33c4-4f9c-9642-9e83d33647d4', 'Who is the current President of the United States?', 'Joe Biden', 'Donald Trump', 'Barack Obama', 'Joe Biden', 'Medium', 20, true, 75, 'c00be767-f747-4ac8-9e1d-1f7a3f0e4760', NOW(), NOW(), false),
    ('3c3de67f-c4f4-4132-b014-18e225b73c3f', 'Who is the author of the famous novel "Pride and Prejudice"?', 'Jane Austen', 'William Shakespeare', 'Charles Dickens', 'Jane Austen', 'Easy', 10, true, 50, 'c00be767-f747-4ac8-9e1d-1f7a3f0e4760', NOW(), NOW(), false),
    ('ae8e02d5-340d-4e52-bf1d-73639217be45', 'What is the capital of Spain?', 'Madrid', 'Barcelona', 'Rome', 'Madrid', 'Easy', 10, true, 50, 'c00be767-f747-4ac8-9e1d-1f7a3f0e4760', NOW(), NOW(), false),
    ('0b87b6c7-98c7-4c42-8a85-e978d97ec672', 'What is the largest desert in the world?', 'Sahara Desert', 'Gobi Desert', 'Arabian Desert', 'Sahara Desert', 'Medium', 20, true, 75, 'c00be767-f747-4ac8-9e1d-1f7a3f0e4760', NOW(), NOW(), false),
    ('5d2cbfc2-14d8-44f1-84e0-1dbbbf60a5a5', 'Who painted the famous painting "Starry Night"?', 'Vincent van Gogh', 'Leonardo da Vinci', 'Pablo Picasso', 'Vincent van Gogh', 'Hard', 30, true, 100, 'c00be767-f747-4ac8-9e1d-1f7a3f0e4760', NOW(), NOW(), false);

-- Science & Mathematics Category
INSERT INTO public.questions
(id, question_text, answer1, answer2, answer3, correct_answer, difficulty, quest_reward_tokens, rewarded, threshold, category_id, creation_date, update_date, checked_by_admin)
VALUES
    ('7d9c4887-c845-42ed-bb36-4e9e5b0c23ff', 'What is the chemical symbol for gold?', 'Au', 'Ag', 'Fe', 'Au', 'Medium', 20, true, 75, '3014a547-2f15-4c98-a376-b7683d5e9325', NOW(), NOW(), false),
    ('231f9f6c-9013-4a7c-a675-59da6a6b12a4', 'What is the boiling point of water in Celsius?', '100°C', '0°C', '50°C', '100°C', 'Easy', 10, true, 50, '3014a547-2f15-4c98-a376-b7683d5e9325', NOW(), NOW(), false),
    ('e605d946-07a3-48a3-b78f-0324b64c4406', 'What is the largest planet in our solar system?', 'Jupiter', 'Saturn', 'Neptune', 'Jupiter', 'Medium', 20, true, 75, '3014a547-2f15-4c98-a376-b7683d5e9325', NOW(), NOW(), false),
    ('46423f06-7fe6-40eb-b4d4-55bb65619f70', 'What is the chemical symbol for water?', 'H2O', 'CO2', 'NaCl', 'H2O', 'Easy', 10, true, 50, '3014a547-2f15-4c98-a376-b7683d5e9325', NOW(), NOW(), false),
    ('d525e607-dcf1-41dc-90b6-47c30acfb113', 'What is the value of π (pi)?', '3.14159', '3.142', '3.14', '3.14159', 'Easy', 10, true, 50, '3014a547-2f15-4c98-a376-b7683d5e9325', NOW(), NOW(), false);

-- Health Category
INSERT INTO public.questions
(id, question_text, answer1, answer2, answer3, correct_answer, difficulty, quest_reward_tokens, rewarded, threshold, category_id, creation_date, update_date, checked_by_admin)
VALUES
    ('051d92cb-7e68-49a2-83f7-52d595c47e3e', 'What is the main function of the lungs?', 'Respiration', 'Digestion', 'Circulation', 'Respiration', 'Easy', 10, true, 50, '1c924683-72f3-4a7f-bd0d-336aa5b2902a', NOW(), NOW(), false),
    ('0636b70e-865a-4b12-bd2c-cbce80d78192', 'What is the average body temperature of a human in Celsius?', '37°C', '40°C', '30°C', '37°C', 'Easy', 10, true, 50, '1c924683-72f3-4a7f-bd0d-336aa5b2902a', NOW(), NOW(), false),
    ('94c23f2e-50bb-4fd3-8813-16607b5200c9', 'Which organ produces insulin in the human body?', 'Pancreas', 'Liver', 'Kidney', 'Pancreas', 'Medium', 20, true, 75, '1c924683-72f3-4a7f-bd0d-336aa5b2902a', NOW(), NOW(), false),
    ('c0429aa4-9fc3-41a4-8314-c29f5e01eb4f', 'What is the main function of the heart?', 'Pumping blood', 'Digestion', 'Respiration', 'Pumping blood', 'Easy', 10, true, 50, '1c924683-72f3-4a7f-bd0d-336aa5b2902a', NOW(), NOW(), false),
    ('ac9ddcc1-91dc-4914-8163-3fb82a188008', 'Which vitamin is essential for good vision?', 'Vitamin A', 'Vitamin C', 'Vitamin D', 'Vitamin A', 'Easy', 10, true, 50, '1c924683-72f3-4a7f-bd0d-336aa5b2902a', NOW(), NOW(), false);

---------------------
- Education & Reference Category
INSERT INTO public.questions
(id, question_text, answer1, answer2, answer3, correct_answer, difficulty, quest_reward_tokens, rewarded, threshold, category_id, creation_date, update_date, checked_by_admin)
VALUES
('0b176b06-c5c3-4da7-b99a-6e2c8da45a13', 'What is the capital of Canada?', 'Ottawa', 'Toronto', 'Montreal', 'Ottawa', 'Easy', 10, true, 50, '3071582d-c0ad-4213-8060-8527918d558e', NOW(), NOW(), false),
('10b4746d-9c79-49f4-b62d-868708b1ae86', 'What is the largest ocean on Earth?', 'Pacific Ocean', 'Atlantic Ocean', 'Indian Ocean', 'Pacific Ocean', 'Easy', 10, true, 50, '3071582d-c0ad-4213-8060-8527918d558e', NOW(), NOW(), false),
('20a2a2f0-8a34-45ab-b1d5-2fb8c5ed5a6d', 'Who is known as the "Father of Geometry"?', 'Euclid', 'Pythagoras', 'Archimedes', 'Euclid', 'Medium', 20, true, 75, '3071582d-c0ad-4213-8060-8527918d558e', NOW(), NOW(), false),
('344ac862-24cc-4470-8166-d4f7462384ef', 'What is the chemical symbol for iron?', 'Fe', 'Au', 'Ag', 'Fe', 'Easy', 10, true, 50, '3071582d-c0ad-4213-8060-8527918d558e', NOW(), NOW(), false),
('518fa013-fbae-4a3f-bc52-e2fcd78940fd', 'What is the longest river in the world?', 'Nile River', 'Amazon River', 'Yangtze River', 'Nile River', 'Medium', 20, true, 75, '3071582d-c0ad-4213-8060-8527918d558e', NOW(), NOW(), false);

-- Computers & Internet Category
INSERT INTO public.questions
(id, question_text, answer1, answer2, answer3, correct_answer, difficulty, quest_reward_tokens, rewarded, threshold, category_id, creation_date, update_date, checked_by_admin)
VALUES
    ('52970e8e-3203-4189-961e-0563be45a0de', 'What does "HTML" stand for?', 'Hypertext Markup Language', 'Hyper Transfer Mode Language', 'Home Tool Markup Language', 'Hypertext Markup Language', 'Easy', 10, true, 50, 'e32bef0e-79c5-4f09-85ba-783de3aafd69', NOW(), NOW(), false),
    ('69a4b23d-9c19-41ef-97e5-f36b1e110f2c', 'What is the largest social media platform in the world?', 'Facebook', 'Instagram', 'Twitter', 'Facebook', 'Easy', 10, true, 50, 'e32bef0e-79c5-4f09-85ba-783de3aafd69', NOW(), NOW(), false),
    ('80df67dc-0db3-4978-848b-f835fc07eb5a', 'What is the extension of a Python source file?', '.py', '.java', '.cpp', '.py', 'Easy', 10, true, 50, 'e32bef0e-79c5-4f09-85ba-783de3aafd69', NOW(), NOW(), false),
    ('a2d93f40-1c86-4e18-aa44-c33a3f9db3e1', 'Who founded Microsoft?', 'Bill Gates', 'Steve Jobs', 'Mark Zuckerberg', 'Bill Gates', 'Medium', 20, true, 75, 'e32bef0e-79c5-4f09-85ba-783de3aafd69', NOW(), NOW(), false),
    ('e08b13d3-c1c8-47a3-8587-84ab3993ad8e', 'What does "URL" stand for?', 'Uniform Resource Locator', 'Universal Resource Link', 'Unified Resource Locator', 'Uniform Resource Locator', 'Easy', 10, true, 50, 'e32bef0e-79c5-4f09-85ba-783de3aafd69', NOW(), NOW(), false);

-- Sports Category
INSERT INTO public.questions
(id, question_text, answer1, answer2, answer3, correct_answer, difficulty, quest_reward_tokens, rewarded, threshold, category_id, creation_date, update_date, checked_by_admin)
VALUES
    ('c1be3760-eb43-4b16-9de0-1c2b3cf6bace', 'Who won the FIFA World Cup in 2018?', 'France', 'Croatia', 'Brazil', 'France', 'Medium', 20, true, 75, '52393790-e9f0-4025-8e27-b32a044bc2c0', NOW(), NOW(), false),
    ('d2f95bd7-9ed2-45af-9e96-2209df4e6d8d', 'Which country won the most gold medals in the 2016 Summer Olympics?', 'United States', 'China', 'Russia', 'United States', 'Medium', 20, true, 75, '52393790-e9f0-4025-8e27-b32a044bc2c0', NOW(), NOW(), false),
    ('f35cf7ec-5a18-4198-8245-e1f53604f64b', 'Who holds the record for the fastest 100-meter sprint?', 'Usain Bolt', 'Carl Lewis', 'Justin Gatlin', 'Usain Bolt', 'Medium', 20, true, 75, '52393790-e9f0-4025-8e27-b32a044bc2c0', NOW(), NOW(), false),
    ('f5b21e57-d0d1-47e2-b7a2-08694be51dc9', 'Which sport uses the term "birdie"?', 'Golf', 'Tennis', 'Badminton', 'Golf', 'Easy', 10, true, 50, '52393790-e9f0-4025-8e27-b32a044bc2c0', NOW(), NOW(), false),
    ('fce5064b-63ab-45e3-8054-59a85c230b10', 'Who won the Wimbledon Men\'s Singles title in 2021?', 'Novak Djokovic', 'Roger Federer', 'Rafael Nadal', 'Novak Djokovic', 'Medium', 20, true, 75, '52393790-e9f0-4025-8e27-b32a044bc2c0', NOW(), NOW(), false);

-----@@@@@@@@@@@@@@@@@@@@@@
-- Business & Finance Category

INSERT INTO public.questions
(id, question_text, answer1, answer2, answer3, correct_answer, difficulty, quest_reward_tokens, rewarded, threshold, category_id, creation_date, update_date, checked_by_admin)
VALUES
('2a2cf059-e3c5-4a28-9754-64aa08015736', 'What does NASDAQ stand for?', 'National Association of Securities Dealers Automated Quotations', 'New York Stock Exchange', 'North American Securities Dealers Association System', 'National Association of Securities Dealers Automated Quotations', 'Medium', 20, true, 75, 'a515d9e8-a636-465f-964c-6b740222b216', NOW(), NOW(), false),
('53677a69-22f4-4b3b-a5d0-0345608c26c7', 'Who is the founder of Amazon?', 'Jeff Bezos', 'Elon Musk', 'Bill Gates', 'Jeff Bezos', 'Easy', 10, true, 50, 'a515d9e8-a636-465f-964c-6b740222b216', NOW(), NOW(), false),
('6f325be3-7c8d-40a0-bd2b-e30ed475b45c', 'What is the main currency used in the European Union?', 'Euro', 'Pound Sterling', 'US Dollar', 'Euro', 'Easy', 10, true, 50, 'a515d9e8-a636-465f-964c-6b740222b216', NOW(), NOW(), false),
('c8678e0e-0a2d-4f9b-a00c-15823d1a21ae', 'What is the largest stock exchange in the world by market capitalization?', 'New York Stock Exchange', 'NASDAQ', 'London Stock Exchange', 'New York Stock Exchange', 'Medium', 20, true, 75, 'a515d9e8-a636-465f-964c-6b740222b216', NOW(), NOW(), false),
('e0e23580-57f3-40a1-839a-bcf5cd43a49b', 'What is the abbreviation for the United States dollar?', 'USD', 'EUR', 'GBP', 'USD', 'Easy', 10, true, 50, 'a515d9e8-a636-465f-964c-6b740222b216', NOW(), NOW(), false);

-- Entertainment & Music Category
INSERT INTO public.questions
(id, question_text, answer1, answer2, answer3, correct_answer, difficulty, quest_reward_tokens, rewarded, threshold, category_id, creation_date, update_date, checked_by_admin)
VALUES
('62be2061-7a06-4db3-8132-c9a089fc1d57', 'Who played the character of Jack Dawson in the movie Titanic?', 'Leonardo DiCaprio', 'Tom Cruise', 'Brad Pitt', 'Leonardo DiCaprio', 'Medium', 20, true, 75, 'a8425ba5-3d7d-4fb0-a711-8cc548906048', NOW(), NOW(), false),
('67f46f35-f05e-4eb0-b73b-05b27a8c1639', 'Which band released the album The Dark Side of the Moon?', 'Pink Floyd', 'The Beatles', 'Led Zeppelin', 'Pink Floyd', 'Medium', 20, true, 75, 'a8425ba5-3d7d-4fb0-a711-8cc548906048', NOW(), NOW(), false),
('ae7f8d8f-c4e8-4ff2-87fd-cbb90da21a24', 'Who won the Academy Award for Best Actor in 2020?', 'Joaquin Phoenix', 'Leonardo DiCaprio', 'Brad Pitt', 'Joaquin Phoenix', 'Medium', 20, true, 75, 'a8425ba5-3d7d-4fb0-a711-8cc548906048', NOW(), NOW(), false),
('f92b9e45-9bdf-4e56-b4bc-c00b899f1d92', 'Which TV series features characters named Walter White and Jesse Pinkman?', 'Breaking Bad', 'Game of Thrones', 'The Sopranos', 'Breaking Bad', 'Medium', 20, true, 75, 'a8425ba5-3d7d-4fb0-a711-8cc548906048', NOW(), NOW(), false),
('fb8b9306-fae9-4a7c-b9a2-9b98d68e99df', 'Who is known as the "King of Pop"?', 'Michael Jackson', 'Elvis Presley', 'Prince', 'Michael Jackson', 'Easy', 10, true, 50, 'a8425ba5-3d7d-4fb0-a711-8cc548906048', NOW(), NOW(), false);

-- Family & Relationships Category
INSERT INTO public.questions
(id, question_text, answer1, answer2, answer3, correct_answer, difficulty, quest_reward_tokens, rewarded, threshold, category_id, creation_date, update_date, checked_by_admin)
VALUES
('3855b53e-7781-48a5-a72a-d152d71ad459', 'What is the traditional gift for a 25th wedding anniversary?', 'Silver', 'Gold', 'Diamond', 'Silver', 'Easy', 10, true, 50, '16f269d2-af46-4e44-8b32-f13949976457', NOW(), NOW(), false),
('46d9a58b-f9b5-4bb9-b72d-474b8252027e', 'What is the most spoken language in the world?', 'Mandarin Chinese', 'English', 'Spanish', 'Mandarin Chinese', 'Medium', 20, true, 75, '16f269d2-af46-4e44-8b32-f13949976457', NOW(), NOW(), false),
('7f79e94a-e1d0-4ef0-a6e3-15340c526034', 'What is the traditional gift for a 50th wedding anniversary?', 'Gold', 'Silver', 'Diamond', 'Gold', 'Easy', 10, true, 50, '16f269d2-af46-4e44-8b32-f13949976457', NOW(), NOW(), false),
('819c308a-e7ed-4322-9e18-7bc5e09bf0b9', 'What is the primary function of DNA?', 'Store genetic information', 'Produce energy', 'Facilitate cell division', 'Store genetic information', 'Medium', 20, true, 75, '16f269d2-af46-4e44-8b32-f13949976457', NOW(), NOW(), false),
('f74efcd0-9a27-4974-b7e4-b176ca0b93f4', 'What is the capital city of Spain?', 'Madrid', 'Barcelona', 'Valencia', 'Madrid', 'Easy', 10, true, 50, '16f269d2-af46-4e44-8b32-f13949976457', NOW(), NOW(), false);

-- Politics & Government Category
INSERT INTO public.questions
(id, question_text, answer1, answer2, answer3, correct_answer, difficulty, quest_reward_tokens, rewarded, threshold, category_id, creation_date, update_date, checked_by_admin)
VALUES
('250a1560-3eb1-4b20-bc71-ecefdca55cb4', 'Who is the current President of the United States?', 'Joe Biden', 'Donald Trump', 'Barack Obama', 'Joe Biden', 'Easy', 10, true, 50, '8b4d7863-21b6-4d5a-b62f-5236d1f80e0b', NOW(), NOW(), false),
('5a57d10b-76cd-4fc3-87f7-04047836b2da', 'Who is the current Prime Minister of the United Kingdom?', 'Boris Johnson', 'Theresa May', 'David Cameron', 'Boris Johnson', 'Medium', 20, true, 75, '8b4d7863-21b6-4d5a-b62f-5236d1f80e0b', NOW(), NOW(), false),
('6be77308-1e94-4b1a-b6d2-8122a6b9d5e2', 'What is the name of the political party of the current President of France?', 'La République En Marche!', 'Front National', 'Les Républicains', 'La République En Marche!', 'Medium', 20, true, 75, '8b4d7863-21b6-4d5a-b62f-5236d1f80e0b', NOW(), NOW(), false),
('f10ddc9e-6ad4-4905-bf8a-b4b09b87658f', 'Which country has the largest population in the world?', 'China', 'India', 'United States', 'China', 'Medium', 20, true, 75, '8b4d7863-21b6-4d5a-b62f-5236d1f80e0b', NOW(), NOW(), false),
('f8995d78-7e6f-45a5-84ff-7e6dc74b83b9', 'Who is the current Chancellor of Germany?', 'Angela Merkel', 'Olaf Scholz', 'Gerhard Schröder', 'Angela Merkel', 'Medium', 20, true, 75, '8b4d7863-21b6-4d5a-b62f-5236d1f80e0b', NOW(), NOW(), false);


