package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Ужин", 410),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак2", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 29, 10, 0), "Обед", 2400)
        );

        List<UserMealWithExcess> mealsTo;

        System.out.println("filteredByCycles");
        mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.sort(Comparator.comparing(UserMealWithExcess::toString));
        mealsTo.forEach(System.out::println);
        System.out.println();
    }

    public static List<UserMealWithExcess> filteredByCycles(
            List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        Map<LocalDate, Integer> dailyCalories = new HashMap<>();
        meals.forEach(meal -> dailyCalories.merge(meal.getDate(), meal.getCalories(), Integer::sum));

        ArrayList<UserMealWithExcess> filteredMeals = new ArrayList<>(meals.size());
        meals.forEach(meal -> {
            if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
                boolean excess = dailyCalories.get(meal.getDate()) > caloriesPerDay;
                filteredMeals.add(new UserMealWithExcess(meal, excess));
            }
        });
        filteredMeals.trimToSize();

        return filteredMeals;
    }
}
