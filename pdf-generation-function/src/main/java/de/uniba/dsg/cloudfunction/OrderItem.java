package de.uniba.dsg.cloudfunction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private double price;
    private int quantity;
    private String beverageName;
    private String beveragePic;
    private Long beverageId;
}
