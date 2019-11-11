/// <reference types="Cypress" />

declare namespace Cypress {

    interface Chainable {
        /**
         * Custom command for login
         * @example cy.login(user)
        */     

        login(user: User): Chainable<Element>

        
        /**
         * Custom command for create user in DB
         * @example cy.signUp(user)
        */   

        signUp(user: User): Chainable<Element>

    }

}