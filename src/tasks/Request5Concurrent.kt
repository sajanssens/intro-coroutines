package tasks

import contributors.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    // first get the repo's as a coroutine
    val repos = service
        .getOrgReposCallSuspend(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    // for each repo, get the contribs as a coroutine; this returns a list of deferred users
    val deferreds: List<Deferred<List<User>>> = repos.map { repo ->
        async { // on same thread as parent; alternatively use:
            // async(Dispatchers.Default) { // on thread from thread pool
            service.getRepoContributorsCallSuspend(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }
    // this coroutineScope only returns if all deferreds are fulfilled so if all sub coroutines are done:
    val users = deferreds.awaitAll().flatten()

    users.aggregate() // return
}

