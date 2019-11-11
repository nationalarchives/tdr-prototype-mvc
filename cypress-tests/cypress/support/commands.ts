// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --

import 'cypress-file-upload';

Cypress.Commands.add('login', (user: User) => {
  cy.visit('/login')
  cy.get('#username').type(user.email)
  cy.get('#password').type(user.password)
  cy.get('.govuk-button').should('contain', 'Sign In').click()
});

Cypress.Commands.add('signUp', (user: User) => {
  cy.visit('/signUp')
  cy.get('#firstName').type(user.firstName)
  cy.get('#lastName').type(user.lastName)
  cy.get('#email').type(user.email)
  cy.get('#password').type(user.password)
  cy.get('.govuk-button').should('contain', 'Sign Up').click()
});

interface User {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This is will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })
