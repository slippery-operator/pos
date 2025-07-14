import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.util.ValidationUtil;
import com.increff.pos.exception.ApiException;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.ConstraintViolation;
import java.util.Set;

public class TestOrderValidation {
    public static void main(String[] args) {
        try {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            
            OrderItemForm item = new OrderItemForm();
            item.setBarcode("TEST123");
            item.setQuantity(2);
            item.setMrp(-10.0); // Negative price
            
            System.out.println("Testing validation of OrderItemForm with negative MRP...");
            Set<ConstraintViolation<OrderItemForm>> violations = validator.validate(item);
            
            if (violations.isEmpty()) {
                System.out.println("NO VIOLATIONS FOUND - VALIDATION PASSED (UNEXPECTED)");
            } else {
                System.out.println("VIOLATIONS FOUND:");
                for (ConstraintViolation<OrderItemForm> violation : violations) {
                    System.out.println("  - " + violation.getPropertyPath() + ": " + violation.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
