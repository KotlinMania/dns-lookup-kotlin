# dns-lookup-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fdns--lookup--kotlin-blue.svg)](https://github.com/KotlinMania/dns-lookup-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/dns-lookup-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/dns-lookup-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/dns-lookup-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/dns-lookup-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`keeperofdakeys/dns-lookup`](https://github.com/keeperofdakeys/dns-lookup/).

**Original Project:** This port is based on [`keeperofdakeys/dns-lookup`](https://github.com/keeperofdakeys/dns-lookup/). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `keeperofdakeys/dns-lookup`

> The text below is reproduced and lightly edited from [`https://github.com/keeperofdakeys/dns-lookup/`](https://github.com/keeperofdakeys/dns-lookup/). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## dns-lookup
[![Crates.io](https://img.shields.io/crates/v/dns-lookup.svg?maxAge=2592000)](https://crates.io/crates/dns-lookup)

A small wrapper for libc to perform simple DNS lookups.

You can use the `lookup_host` function to get a list of IP Addresses for a
given hostname, and the `lookup_name` function to get the reverse dns entry for
the given IP Address.

PS: If you only need a single result, consider
[ToSocketAddrs](https://doc.rust-lang.org/std/net/trait.ToSocketAddrs.html) in libstd.

The library also includes a safe wrapper for `getaddrinfo` and `getnameinfo`.

[Documentation](https://docs.rs/dns-lookup/)

## Usage

### Simple API

```rust
use dns_lookup::{lookup_host, lookup_addr};

{
  let hostname = "localhost";
  let ips: Vec<std::net::IpAddr> = lookup_host(hostname).unwrap().collect::<Vec<_>>();
  assert!(ips.contains(&"127.0.0.1".parse().unwrap()));
}

{
  let ip: std::net::IpAddr = "127.0.0.1".parse().unwrap();
  let host = lookup_addr(&ip).unwrap();

  // The string "localhost" on unix, and the hostname on Windows.
}
```

### libc API
```rust
{
  extern crate dns_lookup;

  use dns_lookup::{getaddrinfo, AddrInfoHints, SockType};

  fn main() {
    let hostname = "localhost";
    let service = "ssh";
    let hints = AddrInfoHints {
      socktype: SockType::Stream.into(),
      .. AddrInfoHints::default()
    };
    let sockets =
      getaddrinfo(Some(hostname), Some(service), Some(hints))
        .unwrap().collect::<std::io::Result<Vec<_>>>().unwrap();

    for socket in sockets {
      // Try connecting to socket
      println!("{:?}", socket);
    }
  }
}

{
  use dns_lookup::getnameinfo;
  use std::net::{IpAddr, SocketAddr};

  let ip: IpAddr = "127.0.0.1".parse().unwrap();
  let port = 22;
  let socket: SocketAddr = (ip, port).into();

  let (name, service) = match getnameinfo(&socket, 0) {
    Ok((n, s)) => (n, s),
    Err(e) => panic!("Failed to lookup socket {:?}", e),
  };

  println!("{:?} {:?}", name, service);
  let _ = (name, service);
}

{
  use dns_lookup::gethostname;

  let hostname = gethostname().unwrap();
}
```

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:dns-lookup-kotlin:0.1.0-SNAPSHOT")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`keeperofdakeys/dns-lookup`](https://github.com/keeperofdakeys/dns-lookup/). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the dns-lookup authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`keeperofdakeys/dns-lookup`](https://github.com/keeperofdakeys/dns-lookup/) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
