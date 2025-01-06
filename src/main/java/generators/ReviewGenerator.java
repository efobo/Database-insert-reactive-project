package generators;

import entities.Review;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

@RequiredArgsConstructor
public class ReviewGenerator extends AbstractGenerator<Review> {
    private final LocalDate releaseDate;

    @Override
    public Review generate() {
        return new Review(
                id++,
                faker.timeAndDate()
                        .between(releaseDate.atStartOfDay().toInstant(ZoneOffset.UTC), Instant.now())
                        .atZone(ZoneId.systemDefault()),
                faker.internet().username(),
                faker.number().numberBetween(1, 6)
        );
    }
}
