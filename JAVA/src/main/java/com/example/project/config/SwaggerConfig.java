package com.example.project.config;

/**
 * Defines OpenAPI/Swagger documentation for the WASAC billing API.
 */

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

	@Value("${server.port:8080}")
	private String serverPort;

	@Bean
	public OpenAPI openAPI() {
		final String schemeName = "bearerAuth";
		return new OpenAPI()
				.info(new Info()
						.title("WASAC Utility Billing System API")
						.description("""
								REST API for the **WASAC (Water and Sanitation Corporation) Utility Billing System** — \
								customer signup, meter readings, tariffs, bill approval, payments, and notifications.

								## Default admin credentials
								| Role | Email | Password |
								|------|-------|----------|
								| Admin | admin@wasac.com | admin123 |

								## Authentication
								1. **Customer signup** — `POST /api/auth/register` (no role field; system assigns ROLE_CUSTOMER)
								2. **Verify OTP** — `POST /api/otp/verify` (OTP logged to console in dev)
								3. **Login** — `POST /api/auth/login` → copy `accessToken`
								4. Click **Authorize** and enter: `Bearer <accessToken>`
								5. **Admin staff registration** — same register endpoint with `role` (ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE) while logged in as admin

								## Main workflow
								Customer signup → login → admin creates customer/meter → admin creates tariff → \
								operator captures reading → bill generated (auto or `POST /api/bills/generate/{readingId}`) → \
								operator captures reading → bill generated (customer emailed) → finance approves bill \
								→ customer pays at WASAC → finance records payment (customer emailed when fully paid)

								## Key endpoints
								- Customers: `/api/customers` (admin CRUD, cascade delete)
								- Meters: `/api/meters` (admin create, admin/operator view)
								- Tariffs: `/api/tariffs`, `/api/tariffs/active/{meterType}`
								- Readings: `/api/readings`
								- Bills: `/api/bills`, `/api/bills/{id}/approve`, `/api/bills/customer/{customerId}`
								- Payments: `/api/payments`, `/api/payments/customer/{customerId}`, `/api/payments/bill/{billId}`
								- Notifications: `/api/notifications`, `/api/notifications/{id}/read`

								## Bill statuses
								PENDING → APPROVED → PARTIALLY_PAID / PAID / OVERDUE (late penalty applied when overdue)

								## Roles
								- **ROLE_ADMIN** — customers, meters, tariffs, staff registration (can also approve bills/record payments)
								- **ROLE_OPERATOR** — meter readings, view meters, generate bills
								- **ROLE_FINANCE** — approve bills (`PUT /api/bills/{id}/approve`), record payments (`POST /api/payments`)
								- **ROLE_CUSTOMER** — view own bills and notifications
								""")
						.version("1.0.0")
						.contact(new Contact().name("WASAC Billing Team").email("admin@wasac.com")))
				.servers(List.of(new Server().url("http://localhost:" + serverPort).description("Local WASAC API")))
				.addSecurityItem(new SecurityRequirement().addList(schemeName))
				.components(new Components().addSecuritySchemes(schemeName,
						new SecurityScheme()
								.name(schemeName)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("JWT access token from POST /api/auth/login")));
	}
}
