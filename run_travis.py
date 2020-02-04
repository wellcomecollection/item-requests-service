#!/usr/bin/env python

import os
import subprocess


def make(task):
    subprocess.check_call(["make", task])


def main():
    travis_event_type = os.environ["TRAVIS_EVENT_TYPE"]
    travis_build_stage = os.environ["TRAVIS_BUILD_STAGE_NAME"]

    try:
        # If it's not an sbt task, we always run it no matter what.
        task = os.environ["TASK"]
    except KeyError:
        sbt_project_name = os.environ["SBT_PROJECT"]
        task = "%s-test" % sbt_project_name

    make(task)

    if travis_event_type == "push" and travis_build_stage == "Services":
        make(task.replace("-test", "-publish"))


if __name__ == "__main__":
    main()
