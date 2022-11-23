package org.kkrasowski.exchange.application.resources

import arrow.core.getOrHandle
import org.kkrasowski.exchange.domain.account.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/accounts")
class UserAccountResource(private val newUserAccountUseCase: NewUserAccountUseCase) {

    /*
     * I did not use the bean validation mechanism, because I decided that validation is a part of the domain, and all failure result should be
     * handled in one place. Another reason is that some of the validations I can do only in the domain with access to repositories etc.
     */
    @RequestMapping(
        method = [RequestMethod.POST],
        consumes = ["application/vnd.new-account.v1+json"],
        produces = ["application/vnd.constraint-violation.v1+json"]
    )
    fun create(@RequestBody request: CreateUserAccountRequest): ResponseEntity<*> {
        return newUserAccountUseCase
            .create(request)
            .map { it.toResponseEntity() }
            .getOrHandle { it.toResponseEntity() }
    }

    private fun AccountId.toResponseEntity() = ResponseEntity.status(201)
        .header("Location", "/accounts/$value")
        .build<Nothing>()

    private fun CreateAccountUseCaseFailure.toResponseEntity() = when (this) {
        is CreateAccountUseCaseFailure.InvalidRequest -> toUnprocessableEntity()
        CreateAccountUseCaseFailure.Failure -> ResponseEntity.internalServerError().build()
    }

    private fun CreateAccountUseCaseFailure.InvalidRequest.toUnprocessableEntity() = ResponseEntity.status(422)
        .header("Content-Type", "application/vnd.constraint-violation.v1+json")
        .body(violation)
}
