#!/usr/bin/env kotlin

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.14.0")

import io.github.typesafegithub.workflows.actions.actions.CheckoutV4
import io.github.typesafegithub.workflows.actions.actions.SetupJavaV4
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV3
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.Release
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile

val workflow = workflow(
    name = "CI Build",
    on = listOf(
        PullRequest(
            branches = listOf("main")
        ),
        Push(
            branches = listOf("main")
        ),
        Release()

    ),
    sourceFile = __FILE__.toPath(),
    targetFileName = "pr_master.yaml",
) {
    job(
        id = "build-and-test",
        name = "Build And Test",
        runsOn = RunnerType.UbuntuLatest,
        timeoutMinutes = 30,
    ) {
        uses(
            name = "Checkout",
            action = CheckoutV4()
        )
        run(
            name = "git log",
            command = "git log -n 20"
        )
        uses(
            name = "setup java",
            action = SetupJavaV4(
                javaVersion = "17",
                distribution = SetupJavaV4.Distribution.Adopt,
                cache = SetupJavaV4.BuildPlatform.Gradle,
            )
        )
        uses(
            name = "build with gradle",
            action = GradleBuildActionV3(
                arguments = "clean check build -PdoFailFast -PdockerComposeTestsEnabled=true --scan",
            )
        )

    }
}

workflow.writeToFile(addConsistencyCheck = true)

