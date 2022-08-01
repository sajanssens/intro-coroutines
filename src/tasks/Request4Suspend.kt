package tasks

import contributors.*

// Our code with suspend functions looks surprisingly similar to the "blocking" version. It's readable and expresses exactly what we're trying to achieve.
// The difference is that instead of blocking the thread, we suspend the coroutine.

// we use GitHubServiceSuspend now
// fun should be `suspend`
suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {
    val repos = service
        .getOrgReposCallSuspend(req.org) // returns Reponse, so no execute() here
        .also { logRepos(req, it) }
        .bodyList()

    return repos.flatMap { repo ->
        service
            .getRepoContributorsCallSuspend(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
    }.aggregate()

}
