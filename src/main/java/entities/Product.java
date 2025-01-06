package entities;

import enums.Country;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
public class Product {
    private int id;
    private double price; // примитив
    private String name; // строка
    private LocalDate releaseDate; // дата
    private Country country; // enum
    private Manufacturer manufacturer; // record
    private List<Review> reviews; // коллекция

    public List<Review> getReviewsWithDelay(long delay) {
        try {
            Thread.sleep(delay);
            return getReviews();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}