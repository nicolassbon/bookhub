package com.bookhub.identity.web.admin;

import com.bookhub.identity.application.admin.ChangeUserRoleService;
import com.bookhub.identity.application.admin.ListUsersService;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRole;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

  private final ListUsersService listUsersService;
  private final ChangeUserRoleService changeUserRoleService;

  public AdminUserController(
      final ListUsersService listUsersService,
      final ChangeUserRoleService changeUserRoleService) {
    this.listUsersService = listUsersService;
    this.changeUserRoleService = changeUserRoleService;
  }

  @GetMapping
  public ResponseEntity<PagedAdminUserResponse> listUsers(
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size,
      @RequestParam(required = false) final String role) {

    final UserRole roleFilter = role != null ? parseRole(role) : null;
    final ListUsersService.PagedUsersResult result = listUsersService.list(page, size, roleFilter);

    final List<AdminUserResponse> items =
        result.users().stream().map(AdminUserResponse::from).toList();

    return ResponseEntity.ok(
        new PagedAdminUserResponse(
            items, result.page(), result.size(), result.totalElements(), result.totalPages()));
  }

  @PatchMapping("/{userId}/role")
  public ResponseEntity<AdminUserResponse> changeRole(
      @PathVariable final UUID userId, @Valid @RequestBody final ChangeRoleRequest request) {

    final UserRole newRole = parseRole(request.role());
    final User updated = changeUserRoleService.changeRole(userId, newRole);
    return ResponseEntity.ok(AdminUserResponse.from(updated));
  }

  private UserRole parseRole(final String role) {
    try {
      return UserRole.valueOf(role.toUpperCase());
    } catch (final IllegalArgumentException e) {
      throw new InvalidRoleException(role);
    }
  }
}
