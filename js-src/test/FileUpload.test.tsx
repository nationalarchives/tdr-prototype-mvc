import { render, RenderResult, waitForElement } from '@testing-library/react'
import { FileUpload } from '../components/FileUpload';
import * as React from "react";
import * as auth from "../aws/auth"

describe("<FileUpload/>", () => {
    it("should render without crashing", () => {
        const upload: RenderResult = render(
            <FileUpload />
        )
        expect(upload).not.toBeNull()
    })

    it("should show an authenticating message on first render", () => {
        const { getAllByText } = render(<FileUpload />)
        getAllByText("Authenticating user...")
    })

    it("should call the authenticate user function", async () => {
        window.history.pushState({}, '', '/?code=abcde');
        const spy: jest.SpyInstance<Promise<void>> = jest.spyOn(auth, "authenticateUser").mockResolvedValue()
        render(<FileUpload />)
        expect(spy).toHaveBeenCalled()
    })

    it("should show the file upload for an authenticated user", async () => {
        window.history.pushState({}, '', '/?code=abcde');
        jest.spyOn(auth, "authenticateUser").mockResolvedValue()
        const { getByText } = render(<FileUpload />)
        await waitForElement(() => getByText("Upload"))
    })

    it("should show an error for an unauthenticated user", async () => {
        window.history.pushState({}, '', '/?code=abcde');
        const spy: jest.SpyInstance<Promise<void>> = jest.spyOn(auth, "authenticateUser").mockRejectedValue("Error")
        expect(spy).toHaveBeenCalled()
        const { getAllByText } = render(<FileUpload />)
        await waitForElement(() => getAllByText("Authenticating user..."))

    })

})