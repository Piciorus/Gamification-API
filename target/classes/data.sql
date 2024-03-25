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

-- Inserting data into the questions table
INSERT INTO questions (question_text, answer1, answer2, answer3, correct_answer, difficulty, quest_reward_tokens, rewarded, threshold,checked_by_admin, category_id, creation_date, update_date)
VALUES
    ('What is the capital of France?', 'Paris', 'Madrid', 'Berlin', 'Paris', 'Easy', 100, false, 50,false, (SELECT id FROM categories WHERE name = 'Category 1'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('What is the largest mammal?', 'Elephant', 'Blue Whale', 'Giraffe', 'Blue Whale', 'Medium', 150, false, 75,true, (SELECT id FROM categories WHERE name = 'Category 2'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Who wrote "To Kill a Mockingbird"?', 'Harper Lee', 'Stephen King', 'J.K. Rowling', 'Harper Lee', 'Hard', 200, false, 100,false, (SELECT id FROM categories WHERE name = 'Category 3'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
