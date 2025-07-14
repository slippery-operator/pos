import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.util.ValidationUtil;
import com.increff.pos.exception.ApiException;
import java.util.Arrays;

public class TestValidation {
    public static void main(String[] args) {
        OrderItemForm item = new OrderItemForm();
        item.setBarcode("TEST123");
        item.setQuantity(2);
        item.setMrp(-10.0); // Negative price
        
        ValidationUtil validationUtil = new ValidationUtil();
        try {
            validationUtil.validateForms(Arrays.asList(item));
            System.out.println("NO EXCEPTION THROWN - VALIDATION FAILED");
        } catch (ApiException e) {
            System.out.println("EXCEPTION CAUGHT: " + e.getMessage());
        }
    }
}
