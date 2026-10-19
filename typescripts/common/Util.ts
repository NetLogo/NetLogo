const switchMap = <T, U>(target: T, map: Map<T, U>, orElse: (target: T) => U): U => {
  return map.has(target) ? unsafe(map.get(target)) : orElse(target)
}

/* eslint-disable @typescript-eslint/no-non-null-assertion */
const unsafe = <T>(x: T | undefined | null): T => x!
/* eslint-enable @typescript-eslint/no-non-null-assertion */

export { switchMap, unsafe }
