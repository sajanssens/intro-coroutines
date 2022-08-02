package tasks

import contributors.*

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    // Based on Request4Suspend

    var allUsers = emptyList<User>() // new: keep track of intermediate results to update them in the UI

    // same
    val repos = service
        .getOrgReposCallSuspend(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    // almost same, only now we keep track of intermediate results
    for ((index, repo) in repos.withIndex()) {
        val users = service.getRepoContributorsCallSuspend(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()

        allUsers = (allUsers + users).aggregate() // update intermediate results
        updateResults(allUsers, index == repos.lastIndex) // update them in the UI and stop when index == last
    }
}
