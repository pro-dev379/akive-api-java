package net.akive.api.auth.application.login.required;

import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.common.domain.Email;

public interface LoadEmailLoginUserPort {

    LoginUser loadByEmailAndLeaveFalse(Email email);
}
