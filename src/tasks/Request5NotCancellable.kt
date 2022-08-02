package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrentNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val repos = service
        .getOrgReposCallSuspend(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val deferredUsers: List<Deferred<List<User>>> = repos.map { repo ->
        GlobalScope.async(Dispatchers.Default) {
            log("starting loading for ${repo.name}")
            delay(3000) // so that we have enough time to cancel the loading
            service.getRepoContributorsCallSuspend(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }

    return deferredUsers.awaitAll().flatten().aggregate()
}
