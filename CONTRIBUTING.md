# Contributing to the IDS-Messaging-Services

The following is a set of guidelines for contributing to the IDS-Messaging-Services. You are very welcome to contribute 
to this project when you find a bug, want to suggest an improvement, or have an idea for a useful 
feature or even would like to contribute code. For this, always create an issue and a corresponding branch, and follow our style 
guides as described below.

## Changelog

We document changes in the [CHANGELOG.md](CHANGELOG.md) on root level which is formatted and 
maintained according to the rules documented on http://keepachangelog.com.

## Issues

You always have to create an issue if you want to integrate a bugfix, improvement, or feature. 
Briefly and clearly describe the purpose of your contribution in the corresponding issue. 
The pre-defined [labels](#labels) improve the understanding of your intentions and help to follow 
the scope of your changes. 

**Bug Report**: As mentioned above, bug reports should be submitted as an issue. To give others 
the chance to reproduce the error in order to find a solution as quickly as possible, the report 
should at least include the following information:
* Description: What did you expect and what happened instead?
* Steps to reproduce (system specs included)
* Relevant logs and/or media (optional): e.g. an image

## Labels

Currently, there are mainly two categories of labels, which should allow the classification of issues: `Type` and `Status`.
The `Type` should allow the assignment of the issue to a larger context, whereas the `Status` represents the current status in the processing of the issue.

Type
*  `Type: Bug`
*  `Type: Documentation`
*  `Type: Enhancement`
*  `Type: Feature`
*  `Type: Help Needed`
*  `Type: Idea`
*  `Type: Maintenance`

Status
*  `Status: New Issue`
*  `Status: Under Review`
*  `Status: Confirmed`
*  `Status: In Progress`
*  `Status: On Hold`
*  `Status: Feedback Needed`

## Branches

This repository has a `development` branch in addition to the `main` branch. The idea is to always 
merge other branches into the `development` branch and to push the changes from 
there into the `main` only for releases. This way, the `development` branch is always up to date, 
with the risk of small issues, while the `master` only contains official releases.

After creating an issue yourself or if you want to address an existing issue, you have to create a 
branch with a unique number and name that assigns it to an issue. Therefore, follow the guidelines 
at https://deepsource.io/blog/git-branch-naming-conventions/. After your changes in your branch, update the 
`CHANGELOG.md` with necessary details in that branch. Please use the feature `linked issues` to link 
issues and pull requests. 

## Commits

We encourage all contributors to stick to the commit convention following the specification on 
[Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/). In general, use  the 
imperative in the present tense. A quick overview of the schema:
```
<type>[optional scope]: <description>
[optional body]
[optional footer(s)]
```

Types: `fix`, `feat`, `chore`, `test`, `refactor`, `docs`, `release`. Append `!` for breaking 
changes to a type. 

An example of a very good commit might look like this: `feat![login]: add awesome breaking feature`
