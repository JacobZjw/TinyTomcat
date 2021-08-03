package http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author JwZheng
 * @date 2021/7/27 20:33
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Cookie {
    private String key;
    private String value;
}
