package net.akive.api.auth.application.port.out.login;

import net.akive.api.auth.domain.login.LoginUser;

public interface LoadEmailLoginUserPort {

    LoginUser findByEmailAndLeaveFalse(String email);
}
