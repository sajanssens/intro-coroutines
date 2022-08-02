package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrentCancellable(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos = service
        .getOrgReposCallSuspend(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val deferredUsers: List<Deferred<List<User>>> = repos.map { repo ->
        async {
            log("starting loading for ${repo.name}")
            delay(3000)
            service.getRepoContributorsCallSuspend(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }

    deferredUsers.awaitAll().flatten().aggregate()
}

