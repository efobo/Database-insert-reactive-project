package generators;

import entities.Manufacturer;
import entities.Product;
import enums.Country;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class ProductGenerator extends AbstractGenerator<Product> {
    private final List<Manufacturer> manufacturers;
    private final int reviewCount;

    @Override
    public Product generate() {
        LocalDate releaseDate = faker.timeAndDate().birthday(0, 5);
        ReviewGenerator reviewGenerator = new ReviewGenerator(releaseDate);

        return new Product(
                id++,
                faker.number().randomDouble(2, 1, 100000),
                faker.commerce().productName(),
                releaseDate,
                Country.values()[faker.number().numberBetween(0, Country.values().length)],
                manufacturers.get(faker.number().numberBetween(0, manufacturers.size())),
                reviewGenerator.generateList(reviewCount)
        );
    }
}
