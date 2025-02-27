package com.vikas.quizApp.service;

import com.vikas.quizApp.dao.QuestionDao;
import com.vikas.quizApp.dao.QuizDao;
import com.vikas.quizApp.model.Question;
import com.vikas.quizApp.model.QuestionWrapper;
import com.vikas.quizApp.model.Quiz;
import com.vikas.quizApp.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuizService {

    @Autowired
    QuizDao quizDao;

    @Autowired
    QuestionDao questionDao;

    public ResponseEntity<String> createQuiz(String category, int numQ, String title) {

        List<Question> questions = questionDao.findRandomQuestionByCategory(category, numQ);

        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setQuestions(questions);
        quizDao.save(quiz);

        return new ResponseEntity<>("Success", HttpStatus.OK);

    }


    public ResponseEntity<List<QuestionWrapper>> getQuizQuestion(Integer id) {

        Optional<Quiz> quiz = quizDao.findById(id);
        List<Question> questionsFromDB = quiz.get().getQuestions();
        List<QuestionWrapper> questionsForUser = new ArrayList<>();
        for(Question q : questionsFromDB) {
            QuestionWrapper qw = new QuestionWrapper(q.getId(), q.getQuestionTitle(), q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4());
            questionsForUser.add(qw);
        }

        return new ResponseEntity<>(questionsForUser, HttpStatus.OK);
    }

    public ResponseEntity<Integer> calculateResult(Integer id, List<Response> responses) {

        Optional<Quiz> quizOpt = quizDao.findById(id);
        if (!quizOpt.isPresent()) {
            return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
        }

        Quiz quiz = quizOpt.get();
        List<Question> questions = quiz.getQuestions();

        if (responses == null || responses.isEmpty()) {
            return new ResponseEntity<>(0, HttpStatus.OK);
        }

        // Map questions by their ID for quick lookup
        Map<Integer, String> answerKey = new HashMap<>();
        for (Question q : questions) {
            answerKey.put(q.getId(), q.getRightAnswer());
        }

        // Debugging: Print the expected answers from DB
//        System.out.println("Correct Answers: " + answerKey);

        int right = 0;
        for (Response response : responses) {
//            System.out.println("Processing response: " + response);

            // Check if question ID exists and response is not null
            if (response.getResponse() != null && answerKey.containsKey(response.getId())) {
//                System.out.println("Comparing: " + response.getResponse() + " with " + answerKey.get(response.getId()));

                // Case-insensitive and trimmed comparison
                if (response.getResponse().trim().equalsIgnoreCase(answerKey.get(response.getId()).trim())) {
                    right++;
//                    System.out.println("Correct answer!");
                }
            }
        }

//        System.out.println("Final Score: " + right);
        return new ResponseEntity<>(right, HttpStatus.OK);
    }
}
