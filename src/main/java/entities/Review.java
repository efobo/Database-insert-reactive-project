package entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@ToString
@AllArgsConstructor
public class Review {
    private int id;
    private ZonedDateTime dateTime; // дата и время создания
    private String user; // автор
    private int rating; // оценка от 1 до 5


}
