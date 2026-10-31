# Release guide

## Version bump

1. Edit `gradle.properties` and set `mod_version` to the release number.
2. Update `docs/COMMIT_MESSAGE.txt` with the changelog for the tag.
3. Commit on `main`.

## Building the release jar

Put the VulkanMod jar for the target Minecraft version at `libs/vulkanmod.jar` and build with the
requirement flag, so a missing jar fails the build instead of producing a jar without VulkanMod blur:

```
./gradlew clean build "-Pmotionsuite.requireVk=true"
```

Check that `build/libs/motionsuite-<version>.jar` contains `dev/r3vy/motionsuite/vk/` before uploading.
CI builds need the same jar available at `libs/vulkanmod.jar` (do not commit it to the repository).

## Tagging

Releases are automated by `.github/workflows/release.yml`. Pushing a tag in the
form `v<version>` (for example `v3.4.0`) triggers a clean build, runs the unit
tests and publishes a GitHub release with the mod jar attached. Release notes
are generated from the commits between tags.

```
git tag v3.4.0
git push origin v3.4.0
```

Every regular push to any branch is built by `.github/workflows/build.yml`,
which runs `build` (compilation, tests, jar) and uploads the jars as workflow
artifacts. The tag version does not need to match `mod_version` in name, but
keeping them identical avoids confusion in the release list.

## Adding a Modrinth or CurseForge target later

The release workflow currently publishes only to GitHub Releases, because the
project is not listed on Modrinth or CurseForge yet. Once it is listed, add the
`Kir-Antipov/mc-publish@v3.3` step to `release.yml` right after the build step
and store the platform tokens as repository secrets:

- `MODRINTH_TOKEN` plus the project id from the Modrinth dashboard
- `CURSEFORGE_TOKEN` plus the numeric project id from the CurseForge console

mc-publish reads the loader, Minecraft version and Java requirement from the
build, so only the ids and tokens are needed.
