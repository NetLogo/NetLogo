#!/bin/sh
# Usage: ./navdirs.sh /path/to/parentdir

if [ $# -ne 1 ]; then
    echo "Usage: $0 <directory>"
    exit 1
fi

PARENT="$1"
if [ ! -d "$PARENT" ]; then
    echo "Error: '$PARENT' is not a directory."
    exit 1
fi

# Build list of subdirectories
SUBDIRS=$(find "$PARENT" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' | sort)
set -- $SUBDIRS
TMPFILE=$(mktemp)
echo "$SUBDIRS" | tr ' ' '\n' > "$TMPFILE"

STATEFILE=$(mktemp)
touch "$STATEFILE"

FULL_PARENT=$(realpath "$PARENT")
CURRENT_INDEX=1
TOTAL=$(wc -l < "$TMPFILE")
LAST_CMD=""

show_dir() {

    DIR=$(sed -n "${CURRENT_INDEX}p" "$TMPFILE")
    [ -z "$DIR" ] && echo "No directory selected." && return
    cd "$FULL_PARENT"/"$DIR" || return

    DONE=$(wc -l < "$STATEFILE")
    STATUS="$((DONE + 1)) of $TOTAL"

    echo "\n===== $DIR | $STATUS ====="

}

mark_done() {
    DIR=$(sed -n "${CURRENT_INDEX}p" "$TMPFILE")
    grep -qxF "$DIR" "$STATEFILE" || echo "$DIR" >> "$STATEFILE"
    echo "Marked as completed: $DIR"
    next_unfinished
}

is_done() {
    DIR="$1"
    grep -qxF "$DIR" "$STATEFILE"
}

count_remaining() {
    DONE=$(wc -l < "$STATEFILE")
    REM=$((TOTAL - DONE))
    echo "$REM of $TOTAL remaining"
}

list_status() {
    echo "Completed:"
    sort "$STATEFILE"
    echo
    echo "Not completed:"
    grep -vxFf "$STATEFILE" "$TMPFILE"
}

next_any() {
    if [ $CURRENT_INDEX -lt $TOTAL ]; then
        CURRENT_INDEX=$((CURRENT_INDEX + 1))
    else
        echo "Already at last directory."
    fi
}

prev_any() {
    if [ $CURRENT_INDEX -gt 1 ]; then
        CURRENT_INDEX=$((CURRENT_INDEX - 1))
    else
        echo "Already at first directory."
    fi
}

first_any() {
    CURRENT_INDEX=1
}

last_any() {
    CURRENT_INDEX=$TOTAL
}

first_unfinished() {
    i=1
    while [ $i -le $TOTAL ]; do
        DIR=$(sed -n "${i}p" "$TMPFILE")
        if ! is_done "$DIR"; then
            CURRENT_INDEX=$i
            return
        fi
        i=$((i + 1))
    done
    echo "No unfinished directories found."
}

last_unfinished() {
    i=$TOTAL
    while [ $i -ge 1 ]; do
        DIR=$(sed -n "${i}p" "$TMPFILE")
        if ! is_done "$DIR"; then
            CURRENT_INDEX=$i
            return
        fi
        i=$((i - 1))
    done
    echo "No unfinished directories found."
}

next_unfinished() {
    while [ $CURRENT_INDEX -lt $TOTAL ]; do
        CURRENT_INDEX=$((CURRENT_INDEX + 1))
        DIR=$(sed -n "${CURRENT_INDEX}p" "$TMPFILE")
        if ! is_done "$DIR"; then
            return
        fi
    done
    echo "No unfinished directories ahead."
}

prev_unfinished() {
    while [ $CURRENT_INDEX -gt 1 ]; do
        CURRENT_INDEX=$((CURRENT_INDEX - 1))
        DIR=$(sed -n "${CURRENT_INDEX}p" "$TMPFILE")
        if ! is_done "$DIR"; then
            return
        fi
    done
    echo "No unfinished directories behind."
}

cleanup() {
    rm -f "$TMPFILE" "$STATEFILE"
}
trap cleanup EXIT

# Main interactive loop
while true; do
    show_dir
    printf "\nStandard navigation: first, last, next, prev"
    printf "\nHeedless navigation: first_any, last_any, next_any, prev_any"
    printf "\nOther: count, finish, list, repeat, quit"
    printf "\n\n> "
    read -r CMD || break
    case "$CMD" in
        first) first_unfinished ;;
        last) last_unfinished ;;
        next) next_unfinished ;;
        prev) prev_unfinished ;;
        first_any) first_any ;;
        last_any) last_any ;;
        next_any) next_any ;;
        prev_any) prev_any ;;
        finish) mark_done ;;
        count) count_remaining ;;
        list) list_status ;;
        quit) break ;;
        repeat)
            if [ -n "$LAST_CMD" ]; then
                echo "Repeating: $LAST_CMD"
                eval "$LAST_CMD"
            else
                echo "No previous command to repeat."
            fi
            ;;
        *)
            LAST_CMD="$CMD"
            eval "$CMD"
            ;;
    esac
done
