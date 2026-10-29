{
  description = "Kotlin Android development with Android Studio on NixOS";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs = { nixpkgs, ... }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs {
        inherit system;
        config = {
          allowUnfree = true;
          android_sdk.accept_license = true;
        };
      };

      buildToolsVersion = "36.0.0";
      android = pkgs.androidenv.composeAndroidPackages {
        platformVersions = [ "36" ];
        buildToolsVersions = [ buildToolsVersion ];
        includeSources = true;
        includeEmulator = true;
        includeSystemImages = true;
        systemImageTypes = [ "google_apis" ];
        abiVersions = [ "x86_64" ];
        includeNDK = false;
        includeCmake = false;
      };
      sdk = android.androidsdk;
      sdkRoot = "${sdk}/libexec/android-sdk";
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        packages = [
          (pkgs.android-studio.withSdk sdk)
          sdk
          pkgs.jdk21
        ];

        JAVA_HOME = "${pkgs.jdk21}/lib/openjdk";
        ANDROID_HOME = sdkRoot;
        # Compatibility with tools that still use the deprecated name.
        ANDROID_SDK_ROOT = sdkRoot;

        shellHook = ''
          # Maven's downloaded aapt2 binary cannot run directly on NixOS.
          # Use the Nix-packaged binary for command-line Gradle builds.
          export GRADLE_OPTS="''${GRADLE_OPTS:-} -Dorg.gradle.project.android.aapt2FromMavenOverride=${sdkRoot}/build-tools/${buildToolsVersion}/aapt2"
        '';
      };
    };
}
