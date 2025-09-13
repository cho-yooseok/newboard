// C:\Users\admin\Desktop\freeboard\freeboard\src\main\java\com\example\freeboard\dto\JwtAuthenticationResponse.java
package com.example.freeboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String username;
    private String role;
}