package tasks

import contributors.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        // Based on Request6Progress

        val channel = Channel<List<User>>() // new: create a rendez-vous channel for communication of users between get calls and updateUI
        var allUsers = emptyList<User>()

        // same
        val repos = service
            .getOrgReposCallSuspend(req.org)
            .also { logRepos(req, it) }
            .bodyList()

        // "async and deferredUsers" are replaced by "launch and channel.send(users)"
        for (repo in repos) {
            launch {
                val users = service.getRepoContributorsCallSuspend(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(users) // send on channel
            }
        }

        // update UI when new users are available on the channel
        repeat(repos.size) {
            val users = channel.receive()
            allUsers = (allUsers + users).aggregate()
            updateResults(allUsers, it == repos.lastIndex)
        }
    }
}
