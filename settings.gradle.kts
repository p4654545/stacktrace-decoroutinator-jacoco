rootProject.name = "stacktrace-decoroutinator"
include(
    "stacktrace-decoroutinator-common",
    "stacktrace-decoroutinator-stdlib",

    "stacktrace-decoroutinator-jvm-common",
    "stacktrace-decoroutinator-jvm-agent-common",
    "stacktrace-decoroutinator-jvm-legacy-common",
    "stacktrace-decoroutinator-jvm",
    "stacktrace-decoroutinator-jvm-agent",
    "stacktrace-decoroutinator-jvm-legacy",

    "stacktrace-decoroutinator-android",

    "stacktrace-decoroutinator-noop",

    "jvm-agent-tests",

    "stacktrace-decoroutinator-jvm-agent-lib", //TODO delete
)
