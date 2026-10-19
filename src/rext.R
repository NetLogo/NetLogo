sr.args <- commandArgs(trailingOnly = TRUE)

sr.rExtensionUserDirPath <- sr.args[2]
sr.rExtensionLibraryPath <- file.path(sr.rExtensionUserDirPath, paste0("r-", R.version$major, ".", R.version$minor, "-library"))

sr.debugEnabled = sr.args[3] == "true"

.libPaths(c(.libPaths(), sr.rExtensionLibraryPath))

suppressPackageStartupMessages(require("rjson"))

# A note on R execution environments: Before R-Extension compatibility was introduced, we used a seperate environment
# for running `eval(..., envir = env)` and `assign(..., envir = env)`.  The issue is that some R functions the user
# could run wouldn't know about this environment, such as `source()`, so R-Extension models using it wouldn't work.  We
# could have asked those users to update their models, but I'd like to avoid having them run into weird errors, and I
# think living with our `sr` extension data and functions in the global environment should be fine.  If some other
# conflict comes up we can look into an even fancier way of handling this.  -Jeremy B November 2022

# In
sr.quit_msg <- -1
sr.stmt_msg <- 0
sr.expr_msg <- 1
sr.assn_msg <- 2
sr.expr_stringified_msg <- 3
sr.heartbeat_request_msg <- 4

# In - Custom
sr.set_named_list_msg <- 900
sr.set_data_frame_msg <- 901

# Out
sr.succ_msg <- 0
sr.err_msg <- 1
sr.heartbeat_response_msg <- 4

sr.eval <- function(s) {
  if (sr.debugEnabled) { print(paste("sr.eval() ", s)) }
  eval(parse(text = s), envir = .GlobalEnv)
}

sr.send_error <- function(sock, message, longMessage) {
  err_msg <- list(
    type = sr.err_msg
  , body = list(
      message = message
    , longMessage = paste(longMessage, "\n")
    )
  )
  writeLines(toJSON(err_msg), sock)
}

sr.handle_warning <- function(expr) {
  if (sr.debugEnabled) { print("sr.handle_warning()") }

  tryCatch(expr, warning = function(w) {
    writeLines(paste("Warning from R environment: ", w$message))
  })
}

sr.write_out <- function(sock, type, body) {
  if (sr.debugEnabled) { print("sr.write_out()") }

  out_msg <- list(
    type = type
  , body = body
  )
  writeLines(toJSON(out_msg), sock)
}

sr.statememt <- function(sock, body) {
  if (sr.debugEnabled) { print(paste("sr.statememt() ", body)) }
  sr.handle_warning(expr = { sr.eval(body) })
  sr.write_out(sock, sr.succ_msg, "")
}

sr.expression <- function(sock, body) {
  if (sr.debugEnabled) { print(paste("sr.expression() ", body)) }
  res <- sr.handle_warning(expr = { sr.eval(body) })
  sr.write_out(sock, sr.succ_msg, res)
}

sr.expression_stringified <- function(sock, body) {
  if (sr.debugEnabled) { print(paste("sr.expression_stringified() ", body)) }
  res <- sr.handle_warning(expr = { toString(sr.eval(body)) })
  sr.write_out(sock, sr.succ_msg, res)
}

sr.assignment <- function(sock, body) {
  if (sr.debugEnabled) { print(paste("sr.assignment() ", body)) }
  sr.handle_warning(expr = {
    var_name <- body$varName
    value    <- body$value
    assign(var_name, value, envir = .GlobalEnv)
  })
  sr.write_out(sock, sr.succ_msg, "")
}

sr.set_named_list <- function(sock, body) {
  if (sr.debugEnabled) { print(paste("sr.set_named_list() ", body)) }
  sr.handle_warning(expr = {
    var_name   <- body$varName
    names_vec  <- unlist(body$names)
    named_list <- setNames(body$values, names_vec)
    assign(var_name, named_list, envir = .GlobalEnv)
  })
  sr.write_out(sock, sr.succ_msg, "")
}

sr.set_data_frame <- function(sock, body) {
  if (sr.debugEnabled) { print(paste("sr.set_data_frame() ", body)) }
  sr.handle_warning(expr = {
    var_name <- body$varName
    names    <- body$names
    columns  <- body$columns

    columns_vec <- lapply(columns, unlist)
    data_frame  <- do.call(data.frame, columns_vec)
    colnames(data_frame) <- unlist(names)

    assign(var_name, data_frame, envir = .GlobalEnv)
  })
  sr.write_out(sock, sr.succ_msg, "")
}

sr.heartbeat <- function(sock) {
  if (sr.debugEnabled) { print("sr.heartbeat()") }
  out_msg <- list(type = sr.heartbeat_response_msg)
  writeLines(toJSON(out_msg), sock)
}

sr.quit <- function(sock) {
  if (sr.debugEnabled) { print("sr.quit()") }
  out_msg <- list(type = sr.succ_msg)
  writeLines(toJSON(out_msg), sock)
}

sr.start_server <- function() {
  if (sr.debugEnabled) { print("sr.start_server()") }

  # We should always provide the port for this script to use, this is just so we can load this file with `source()` in
  # an R interpreter for testing purposes.  -Jeremy B November 2022
  port <- ifelse(length(sr.args) == 0, 9000, strtoi(sr.args[1]))
  if (sr.debugEnabled) { print(paste("sr.start_server() port ", port)) }

  sock <- socketConnection(
    host = "localhost"
  , port = port
  , blocking = TRUE
  , server = TRUE
  , open = "r+"
  )

  active <- TRUE
  read_count = 0
  while (active) {
    read_count = read_count + 1
    if (sr.debugEnabled) { print(paste("sr.start_server() read_count ", read_count)) }
    tryCatch({
      msg_line <- readLines(sock, 1)
      if (sr.debugEnabled) { print(paste("sr.start_server() msg_line ", msg_line)) }

      if (length(msg_line) != 0) {
        msg_parsed <- fromJSON(json_str = msg_line, simplify = TRUE)
        msg_type <- msg_parsed$type

        if (msg_type == sr.heartbeat_request_msg) {
          sr.heartbeat(sock)

        } else if (msg_type == sr.stmt_msg) {
          sr.statememt(sock, msg_parsed$body)

        } else if (msg_type == sr.expr_msg) {
          sr.expression(sock, msg_parsed$body)

        } else if (msg_type == sr.assn_msg) {
          sr.assignment(sock, msg_parsed$body)

        } else if (msg_type == sr.expr_stringified_msg) {
          sr.expression_stringified(sock, msg_parsed$body)

        } else if (msg_type == sr.quit_msg) {
          active <- FALSE
          sr.quit(sock)

        } else if (msg_type == sr.set_named_list_msg) {
          sr.set_named_list(sock, msg_parsed$body)

        } else if (msg_type == sr.set_data_frame_msg) {
          sr.set_data_frame(sock, msg_parsed$body)

        } else {
          sr.send_error("Bad message type: ", toString(msg_type), "")
        }
      }
    },

    error = function(e) {
      sr.send_error(sock, e$message, e$message)
    },

    # Warnings need to be handled by the specific message type. If something escapes to here, let's call it a failure to
    # make sure the caller gets something back so it doesn't wait forever for a reply.  -Jeremy B August 2024
    warning = function(w) {
      long_error <- paste("Unexpected warning from R environment, treating as an error: ", w$message)
      sr.send_error(sock, w$message, long_error)
    })
  }

  close(sock)
}

sr.start_server()
