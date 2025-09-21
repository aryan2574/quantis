import {
  ApolloClient,
  InMemoryCache,
  createHttpLink,
  split,
} from "@apollo/client";
import { setContext } from "@apollo/client/link/context";
import { GraphQLWsLink } from "@apollo/client/link/subscriptions";
import { getMainDefinition } from "@apollo/client/utilities";
import { createClient } from "graphql-ws";

// HTTP Link for queries and mutations
const httpLink = createHttpLink({
  uri: "http://localhost:8085/graphql", // Dashboard Gateway GraphQL endpoint
});

// WebSocket Link for subscriptions
const wsLink = new GraphQLWsLink(
  createClient({
    url: "ws://localhost:8085/graphql", // Dashboard Gateway WebSocket endpoint
    connectionParams: () => {
      const token = localStorage.getItem("auth_token");
      return {
        authorization: token ? `Bearer ${token}` : "",
      };
    },
    shouldRetry: (errorOrCloseEvent) => {
      console.log("WebSocket connection error:", errorOrCloseEvent);
      return true;
    },
    retryAttempts: 5,
    retryWait: async (retries) => {
      await new Promise((resolve) => setTimeout(resolve, retries * 1000));
    },
  })
);

// Auth Link for HTTP requests
const authLink = setContext((_, { headers }) => {
  const token = localStorage.getItem("auth_token");
  return {
    headers: {
      ...headers,
      authorization: token ? `Bearer ${token}` : "",
    },
  };
});

// Split link: HTTP for queries/mutations, WebSocket for subscriptions
const splitLink = split(
  ({ query }) => {
    const definition = getMainDefinition(query);
    return (
      definition.kind === "OperationDefinition" &&
      definition.operation === "subscription"
    );
  },
  wsLink,
  authLink.concat(httpLink)
);

export const apolloClient = new ApolloClient({
  link: splitLink,
  cache: new InMemoryCache({
    typePolicies: {
      Query: {
        fields: {
          portfolio: {
            merge: true,
          },
          positions: {
            merge: false,
          },
          orderHistory: {
            merge: false,
          },
          tradingHistory: {
            merge: false,
          },
          recentTrades: {
            merge: false,
          },
          marketSummary: {
            merge: false,
          },
        },
      },
      Portfolio: {
        fields: {
          positions: {
            merge: false,
          },
        },
      },
      MarketData: {
        keyFields: ["symbol"],
      },
      Order: {
        keyFields: ["orderId"],
      },
      Trade: {
        keyFields: ["tradeId"],
      },
      Position: {
        keyFields: ["userId", "symbol"],
      },
    },
  }),
  defaultOptions: {
    watchQuery: {
      errorPolicy: "all",
      fetchPolicy: "cache-and-network",
    },
    query: {
      errorPolicy: "all",
      fetchPolicy: "cache-first",
    },
    mutate: {
      errorPolicy: "all",
    },
  },
  // Suppress DevTools message in development
  connectToDevTools: false,
});
