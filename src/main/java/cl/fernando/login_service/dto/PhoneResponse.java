package cl.fernando.login_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneResponse {
	private Long number;
	private Integer citycode;
	private String countrycode;
}
