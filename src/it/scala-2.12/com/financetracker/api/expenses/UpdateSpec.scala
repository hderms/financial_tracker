package com.financetracker.api.expenses

import org.scalatest._
import org.scalatest.prop._
import org.http4s._
import org.http4s.dsl._
import org.http4s.circe._
import org.http4s.headers._
import org.http4s.client.blaze._
import fs2.interop.cats._
import io.circe._
import io.circe.literal._

import com.financetracker.helpers._
import com.financetracker.data._
import com.financetracker.types._

class UpdateSpec extends FunSpec with Matchers with BeforeAndAfter with PropertyChecks {
  describe("PATCH /users/:id/expenses") {
    describe("admin") {
      it("updates expense for any user") {
        val expected: Json = json"""
          {
            "error" : null,
            "result" : {
              "id" : "ignored",
              "amount": 200,
              "description": "Desc1",
              "comment": "",
              "occuredAt": "2017-01-07 10:52",
              "userId": "ignored",
              "createdAt" : "ignored",
              "updatedAt" : "ignored"
            }
          }      
        """

        val payloadCreate = """
          {
            "amount": 100,
            "description": "Desc",
            "comment": "Comment",
            "occuredAt": "2017-01-05 20:52"
          }
        """


        val payloadUpdate = """
          {
            "amount": 200,
            "description": "Desc1",
            "comment": "",
            "occuredAt": "2017-01-07 10:52"
          }
        """

        async {
          for {
            userWithTokenAndUsers <- loggedInUser(Role.Admin)
            user = userWithTokenAndUsers._3.find(_.role == Role.User).get
            body <- TaskAttempt.liftT(EntityEncoder[String].toEntity(payloadCreate).map(_.body))
            req = Request(
              method = POST, 
              uri = baseUrl / "users" / user.id.value.toString / "expenses",
              body = body,
              headers = authHeader(userWithTokenAndUsers._2)
            )
            responseCreate <- TaskAttempt.liftT(httpClient.fetchAs[Json](req))
            bodyUpdate <- TaskAttempt.liftT(EntityEncoder[String].toEntity(payloadUpdate).map(_.body))
            req1 = Request(
              method = PATCH, 
              uri = baseUrl / "users" / user.id.value.toString / "expenses" / responseCreate.hcursor.downField("result").downField("id").as[Int].right.get.toString,
              body = bodyUpdate,
              headers = authHeader(userWithTokenAndUsers._2)
            )
            response <- TaskAttempt.liftT(httpClient.fetchAs[Json](req1))
          } yield {
            withClue(s"Response: $response, expected: $expected: ") {
              compareJsonsIgnoring(List("id", "createdAt", "updatedAt", "userId"))(response, expected) shouldBe true
            }

          }
        }
      }
    }

    describe("manager") {
      it("fails with 300 for trying to update any user's expense") {
        val expected: Json = json"""
          {
            "error" : {
              "code": 300,
              "message": "Unauthorized"
            },
            "result" : null
          }      
        """

        val payloadUpdate = """
          {
            "amount": 200,
            "description": "Desc1",
            "comment": "",
            "occuredAt": "2017-01-07 10:52"
          }
        """

        async {
          for {
            userWithTokenAndUsers <- loggedInUser(Role.Manager)
            user = userWithTokenAndUsers._3.find(_.role == Role.User).get
            expenses <- env.expenseRepo.all(user.id)
            bodyUpdate <- TaskAttempt.liftT(EntityEncoder[String].toEntity(payloadUpdate).map(_.body))
            req1 = Request(
              method = PATCH, 
              uri = baseUrl / "users" / user.id.value.toString / "expenses" / expenses.head.id.value.toString,
              body = bodyUpdate,
              headers = authHeader(userWithTokenAndUsers._2)
            )
            response <- TaskAttempt.liftT(httpClient.fetchAs[Json](req1))
          } yield {
            withClue(s"Response: $response, expected: $expected: ") {
              compareJsonsIgnoring(List("id", "createdAt", "updatedAt", "userId"))(response, expected) shouldBe true
            }

          }
        }
      }
    }

    describe("user") {
      it("updates expense for himself") {
        val expected: Json = json"""
          {
            "error" : null,
            "result" : {
              "id" : "ignored",
              "amount": 200,
              "description": "Desc1",
              "comment": "",
              "occuredAt": "2017-01-07 10:52",
              "userId": "ignored",
              "createdAt" : "ignored",
              "updatedAt" : "ignored"
            }
          }      
        """

        val payloadCreate = """
          {
            "amount": 100,
            "description": "Desc",
            "comment": "Comment",
            "occuredAt": "2017-01-05 20:52"
          }
        """


        val payloadUpdate = """
          {
            "amount": 200,
            "description": "Desc1",
            "comment": "",
            "occuredAt": "2017-01-07 10:52"
          }
        """

        async {
          for {
            userWithTokenAndUsers <- loggedInUser(Role.User)
            user = userWithTokenAndUsers._3.find(_.role == Role.User).get
            body <- TaskAttempt.liftT(EntityEncoder[String].toEntity(payloadCreate).map(_.body))
            req = Request(
              method = POST, 
              uri = baseUrl / "users" / user.id.value.toString / "expenses",
              body = body,
              headers = authHeader(userWithTokenAndUsers._2)
            )
            responseCreate <- TaskAttempt.liftT(httpClient.fetchAs[Json](req))
            bodyUpdate <- TaskAttempt.liftT(EntityEncoder[String].toEntity(payloadUpdate).map(_.body))
            req1 = Request(
              method = PATCH, 
              uri = baseUrl / "users" / user.id.value.toString / "expenses" / responseCreate.hcursor.downField("result").downField("id").as[Int].right.get.toString,
              body = bodyUpdate,
              headers = authHeader(userWithTokenAndUsers._2)
            )
            response <- TaskAttempt.liftT(httpClient.fetchAs[Json](req1))
          } yield {
            withClue(s"Response: $response, expected: $expected: ") {
              compareJsonsIgnoring(List("id", "createdAt", "updatedAt", "userId"))(response, expected) shouldBe true
            }

          }
        }
      }



      it("fails with 300 for trying to update any user's expense") {
        val expected: Json = json"""
          {
            "error" : {
              "code": 300,
              "message": "Unauthorized"
            },
            "result" : null
          }      
        """

        val payloadUpdate = """
          {
            "amount": 200,
            "description": "Desc1",
            "comment": "",
            "occuredAt": "2017-01-07 10:52"
          }
        """

        async {
          for {
            userWithTokenAndUsers <- loggedInUser(Role.User)
            user = userWithTokenAndUsers._3.find(_.role == Role.Manager).get
            expenses <- env.expenseRepo.all(user.id)
            bodyUpdate <- TaskAttempt.liftT(EntityEncoder[String].toEntity(payloadUpdate).map(_.body))
            req1 = Request(
              method = PATCH, 
              uri = baseUrl / "users" / user.id.value.toString / "expenses" / expenses.head.id.value.toString,
              body = bodyUpdate,
              headers = authHeader(userWithTokenAndUsers._2)
            )
            response <- TaskAttempt.liftT(httpClient.fetchAs[Json](req1))
          } yield {
            withClue(s"Response: $response, expected: $expected: ") {
              compareJsonsIgnoring(List("id", "createdAt", "updatedAt", "userId"))(response, expected) shouldBe true
            }

          }
        }
      }
    }

    describe("anonymous") {
      it("fails with 300 for trying to update any user's expense") {
        val expected: Json = json"""
          {
            "error" : {
              "code": 300,
              "message": "Unauthorized"
            },
            "result" : null
          }      
        """

        val payloadUpdate = """
          {
            "amount": 200,
            "description": "Desc1",
            "comment": "",
            "occuredAt": "2017-01-07 10:52"
          }
        """

        async {
          for {
            userWithTokenAndUsers <- loggedInUser(Role.User)
            user = userWithTokenAndUsers._3.find(_.role == Role.User).get
            expenses <- env.expenseRepo.all(user.id)
            bodyUpdate <- TaskAttempt.liftT(EntityEncoder[String].toEntity(payloadUpdate).map(_.body))
            req1 = Request(
              method = PATCH, 
              uri = baseUrl / "users" / user.id.value.toString / "expenses" / expenses.head.id.value.toString,
              body = bodyUpdate,
            )
            response <- TaskAttempt.liftT(httpClient.fetchAs[Json](req1))
          } yield {
            withClue(s"Response: $response, expected: $expected: ") {
              compareJsonsIgnoring(List("id", "createdAt", "updatedAt", "userId"))(response, expected) shouldBe true
            }

          }
        }
      }

    }



    // describe("manager") {
    //   it("creates expense for himself") {
    //     val expected: Json = json"""
    //       {
    //         "error" : null,
    //         "result" : {
    //           "id" : "ignored",
    //           "amount": 100,
    //           "description": "Desc",
    //           "comment": "Comment",
    //           "occuredAt": "2017-01-05 20:52",
    //           "userId": "ignored",
    //           "createdAt" : "ignored",
    //           "updatedAt" : "ignored"
    //         }
    //       }      
    //     """

    //     val payload = """
    //       {
    //         "amount": 100,
    //         "description": "Desc",
    //         "comment": "Comment",
    //         "occuredAt": "2017-01-05 20:52"
    //       }
    //     """

    //     async {
    //       for {
    //         userWithTokenAndUsers <- loggedInUser(Role.Manager)
    //         user = userWithTokenAndUsers._3.find(_.role == Role.Manager).get
    //         body <- TaskAttempt.liftT(EntityEncoder[String].toEntity(payload).map(_.body))
    //         req = Request(
    //           method = POST, 
    //           uri = baseUrl / "users" / user.id.value.toString / "expenses",
    //           body = body,
    //           headers = authHeader(userWithTokenAndUsers._2)
    //         )
    //         response <- TaskAttempt.liftT(httpClient.fetchAs[Json](req))
    //         fetchReq = Request(
    //           method = GET,
    //           uri = baseUrl / "users" / user.id.value.toString / "expenses" / response.hcursor.downField("result").downField("id").as[Int].right.get.toString(),
    //           headers = authHeader(userWithTokenAndUsers._2)
    //         )
    //         fetchResponse <- TaskAttempt.liftT(httpClient.fetchAs[Json](fetchReq))
    //       } yield {
    //         withClue(s"Response: $response, expected: $expected: ") {
    //           compareJsonsIgnoring(List("id", "createdAt", "updatedAt", "userId"))(response, expected) shouldBe true
    //         }

    //         withClue(s"Fetch response: $fetchResponse, expected: $expected: ") {
    //           compareJsonsIgnoring(List("id", "createdAt", "updatedAt", "userId"))(fetchResponse, expected) shouldBe true
    //         }

    //       }
    //     }
    //   }
    // }

    // describe("user") {
    //   it("creates expense for himself") {
    //     val expected: Json = json"""
    //       {
    //         "error" : null,
    //         "result" : {
    //           "id" : "ignored",
    //           "amount": 100,
    //           "description": "Desc",
    //           "comment": "Comment",
    //           "occuredAt": "2017-01-05 20:52",
    //           "userId": "ignored",
    //           "createdAt" : "ignored",
    //           "updatedAt" : "ignored"
    //         }
    //       }      
    //     """

    //     val payload = """
    //       {
    //         "amount": 100,
    //         "description": "Desc",
    //         "comment": "Comment",
    //         "occuredAt": "2017-01-05 20:52"
    //       }
    //     """

    //     async {
    //       for {
    //         userWithTokenAndUsers <- loggedInUser(Role.User)
    //         user = userWithTokenAndUsers._3.find(_.role == Role.User).get
    //         body <- TaskAttempt.liftT(EntityEncoder[String].toEntity(payload).map(_.body))
    //         req = Request(
    //           method = POST, 
    //           uri = baseUrl / "users" / user.id.value.toString / "expenses",
    //           body = body,
    //           headers = authHeader(userWithTokenAndUsers._2)
    //         )
    //         response <- TaskAttempt.liftT(httpClient.fetchAs[Json](req))
    //         fetchReq = Request(
    //           method = GET,
    //           uri = baseUrl / "users" / user.id.value.toString / "expenses" / response.hcursor.downField("result").downField("id").as[Int].right.get.toString(),
    //           headers = authHeader(userWithTokenAndUsers._2)
    //         )
    //         fetchResponse <- TaskAttempt.liftT(httpClient.fetchAs[Json](fetchReq))
    //       } yield {
    //         withClue(s"Response: $response, expected: $expected: ") {
    //           compareJsonsIgnoring(List("id", "createdAt", "updatedAt", "userId"))(response, expected) shouldBe true
    //         }

    //         withClue(s"Fetch response: $fetchResponse, expected: $expected: ") {
    //           compareJsonsIgnoring(List("id", "createdAt", "updatedAt", "userId"))(fetchResponse, expected) shouldBe true
    //         }

    //       }
    //     }
    //   }

    //   it("fails with 300 for any other user") {
    //     val expected: Json = json"""
    //       {
    //         "error" : {
    //           "code": 300,
    //           "message": "Unauthorized"
    //         },
    //         "result" : null
    //       }      
    //     """

    //     val payload = """
    //       {
    //         "amount": 100,
    //         "description": "Desc",
    //         "comment": "Comment",
    //         "occuredAt": "2017-01-05 20:52"
    //       }
    //     """

    //     async {
    //       for {
    //         userWithTokenAndUsers <- loggedInUser(Role.User)
    //         user = userWithTokenAndUsers._3.find(_.role == Role.Manager).get
    //         body <- TaskAttempt.liftT(EntityEncoder[String].toEntity(payload).map(_.body))
    //         req = Request(
    //           method = POST, 
    //           uri = baseUrl / "users" / user.id.value.toString / "expenses",
    //           body = body,
    //           headers = authHeader(userWithTokenAndUsers._2)
    //         )
    //         response <- TaskAttempt.liftT(httpClient.fetchAs[Json](req))
    //       } yield {
    //         withClue(s"Response: $response, expected: $expected: ") {
    //           compareJsonsIgnoring(List("id", "createdAt", "updatedAt", "userId"))(response, expected) shouldBe true
    //         }
    //       }
    //     }
    //   }

    // }


    // describe("anonymous") {
    //   it("fails with 300 for any user") {
    //     val expected: Json = json"""
    //       {
    //         "error" : {
    //           "code": 300,
    //           "message": "Unauthorized"
    //         },
    //         "result" : null
    //       }      
    //     """

    //     val payload = """
    //       {
    //         "amount": 100,
    //         "description": "Desc",
    //         "comment": "Comment",
    //         "occuredAt": "2017-01-05 20:52"
    //       }
    //     """

    //     async {
    //       for {
    //         userWithTokenAndUsers <- loggedInUser(Role.User)
    //         user = userWithTokenAndUsers._3.find(_.role == Role.Manager).get
    //         body <- TaskAttempt.liftT(EntityEncoder[String].toEntity(payload).map(_.body))
    //         req = Request(
    //           method = POST, 
    //           uri = baseUrl / "users" / user.id.value.toString / "expenses",
    //           body = body,
    //         )
    //         response <- TaskAttempt.liftT(httpClient.fetchAs[Json](req))
    //       } yield {
    //         withClue(s"Response: $response, expected: $expected: ") {
    //           compareJsonsIgnoring(List("id", "createdAt", "updatedAt", "userId"))(response, expected) shouldBe true
    //         }
    //       }
    //     }
    //   }
    // }
  }
}