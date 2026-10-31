package com.exam.repository;

import com.exam.exam.Category;
import com.exam.exam.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz,Long> {
    public List<Quiz> findByCategory(Category category);

    @Query("select distinct q from Quiz q left join q.category c "
            + "where lower(coalesce(q.title, '')) like lower(concat('%', :term, '%')) "
            + "or lower(coalesce(q.description, '')) like lower(concat('%', :term, '%')) "
            + "or lower(coalesce(c.title, '')) like lower(concat('%', :term, '%'))")
    List<Quiz> searchByText(@Param("term") String term);

   // for user
    public List<Quiz> findByActive(Boolean b);

    public List<Quiz> findByCategoryAndActive(Category c, Boolean b);

}
