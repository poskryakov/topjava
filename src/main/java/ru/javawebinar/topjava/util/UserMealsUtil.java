package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

        System.out.println("filteredByStreams");
        mealsTo = filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.sort(Comparator.comparing(UserMealWithExcess::toString));
        mealsTo.forEach(System.out::println);
        System.out.println();

        System.out.println("filteredByCyclesSinglePass");
        mealsTo = filteredByCyclesSinglePass(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.sort(Comparator.comparing(UserMealWithExcess::toString));
        mealsTo.forEach(System.out::println);
        System.out.println();

        System.out.println("filteredByStreamsSinglePass");
        mealsTo = filteredByStreamsSinglePass(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
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

    public static List<UserMealWithExcess> filteredByStreams(
            List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        Map<LocalDate, Integer> dailyCalories = meals.stream()
                .collect(Collectors.groupingBy(UserMeal::getDate, Collectors.summingInt(UserMeal::getCalories)));

        return meals.stream()
                .filter(meal -> TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime))
                .map(meal -> new UserMealWithExcess(meal, dailyCalories.get(meal.getDate()) > caloriesPerDay))
                .collect(Collectors.toCollection(ArrayList::new));
    }


    public static List<UserMealWithExcess> filteredByCyclesSinglePass(
            List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        SinglePassContainer container = new SinglePassContainer(startTime, endTime, caloriesPerDay);
        for (UserMeal meal : meals) {
            container.accumulate(meal);
        }
        return container.finish();
    }

    public static List<UserMealWithExcess> filteredByStreamsSinglePass(
            List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        Collector<UserMeal, SinglePassContainer, List<UserMealWithExcess>> singlePassCollector = Collector.of(
                () -> new SinglePassContainer(startTime, endTime, caloriesPerDay),
                SinglePassContainer::accumulate,
                SinglePassContainer::combine,
                SinglePassContainer::finish,
                Collector.Characteristics.UNORDERED
        );

        return meals.stream().collect(singlePassCollector);
    }

    private static class SinglePassContainer {
        private LocalTime startTime;
        private LocalTime endTime;
        private int caloriesPerDay;

        private Set<UserMealWithExcess> filteredMeals;
        private Map<LocalDate, Integer> dailyCalories;
        private Map<LocalDate, Set<UserMealWithExcess>> excessDump;
        private Map<LocalDate, Set<UserMealWithExcess>> deficientDump;

        public SinglePassContainer (LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.caloriesPerDay = caloriesPerDay;

            this.filteredMeals = new HashSet<>();
            this.dailyCalories = new HashMap<>();
            this.excessDump = new HashMap<>();
            this.deficientDump = new HashMap<>();
        }

        private void accumulate(UserMeal meal) {
            int dateCaloriesOld = dailyCalories.getOrDefault(meal.getDate(), 0);
            int dateCaloriesNew = dailyCalories.merge(meal.getDate(), meal.getCalories(), Integer::sum);

            if (TimeUtil.isBetweenHalfOpen(meal.getTime(), startTime, endTime)) {
                if (dateCaloriesNew <= caloriesPerDay) {
                    excessDump.putIfAbsent(meal.getDate(), new LinkedHashSet<>());
                    deficientDump.putIfAbsent(meal.getDate(), new LinkedHashSet<>());

                    UserMealWithExcess deficientMeal = new UserMealWithExcess(meal, false);
                    UserMealWithExcess excessMeal = new UserMealWithExcess(meal, true);

                    excessDump.get(meal.getDate()).add(excessMeal);
                    deficientDump.get(meal.getDate()).add(deficientMeal);
                    filteredMeals.add(deficientMeal);
                } else {
                    filteredMeals.add(new UserMealWithExcess(meal, true));
                }
            }

            if (dateCaloriesOld <= caloriesPerDay && dateCaloriesNew > caloriesPerDay) {
                filteredMeals.removeAll(deficientDump.getOrDefault(meal.getDate(), Collections.emptySet()));
                filteredMeals.addAll(excessDump.getOrDefault(meal.getDate(), Collections.emptySet()));
                deficientDump.remove(meal.getDate());
                excessDump.remove(meal.getDate());
            }
        }

        // Never invoked in my code
        private SinglePassContainer combine(SinglePassContainer container) {
            return null;
        }

        private List<UserMealWithExcess> finish() {
            return new ArrayList<>(filteredMeals);
        }
    }
}
